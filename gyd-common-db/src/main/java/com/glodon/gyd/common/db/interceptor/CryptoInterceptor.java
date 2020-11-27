package com.glodon.gyd.common.db.interceptor;

import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.glodon.gyd.common.db.util.AesUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
              @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }) })
public class CryptoInterceptor implements Interceptor {

    private final Map<Class<?>, List<EntityField>> entityFieldsCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        if (SqlCommandType.SELECT == ms.getSqlCommandType()) {
            return decrypt(invocation);
        } else {
            return encrypt(invocation);
        }
    }

    private Object encrypt(Invocation invocation) throws Exception {
        Object parameter = invocation.getArgs()[1];
        doEncrypt(parameter);
        return invocation.proceed();
    }

    private void doEncrypt(Object object) throws Exception {
        if (Objects.isNull(object)) {
            return;
        }
        if (object instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) object;
            encryptFields(paramMap.getOrDefault("param1", null));
            encryptFields(paramMap.getOrDefault("et", null));
        } else {
            encryptFields(object);
        }
    }

    private void encryptFields(Object object) throws Exception {
        if (Objects.isNull(object)) {
            return;
        }
        List<EntityField> entityFields = getCryptoFields(object.getClass());
        for (EntityField field : entityFields) {
            if (field.isEncrypt()) {
                String value = BeanUtils.getProperty(object, field.getField().getName());
                if (StringUtils.isNotBlank(value)) {
                    BeanUtils.setProperty(object, field.getField().getName(), AesUtil.encrypt(value));
                }
            }
        }
    }

    private Object decrypt(Invocation invocation) throws Exception {
        Object result = invocation.proceed();
        if (result instanceof Collection) {
            @SuppressWarnings("rawtypes")
            Collection collection = (Collection) result;
            for (Object aCollection : collection) {
                doDecrypt(aCollection);
            }
        } else {
            doDecrypt(result);
        }
        return result;
    }

    private void doDecrypt(Object object) throws Exception {
        if (Objects.isNull(object)) {
            return;
        }
        List<EntityField> entityFields = getCryptoFields(object.getClass());
        for (EntityField field : entityFields) {
            if (field.isDecrypt()) {
                String value = BeanUtils.getProperty(object, field.getField().getName());
                if (StringUtils.isNotBlank(value)) {
                    BeanUtils.setProperty(object, field.getField().getName(), AesUtil.decrypt(value));
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        // to do nothing
    }

    private List<EntityField> getCryptoFields(Class<?> parameterClass) {
        if (entityFieldsCache.containsKey(parameterClass)) {
            return entityFieldsCache.get(parameterClass);
        }
        List<Field> fields = ReflectionKit.getFieldList(parameterClass);
        List<EntityField> cryptoFields = fields.stream().filter(field -> field.isAnnotationPresent(Crypto.class))
                .map(field -> {
                    Crypto crypto = field.getAnnotation(Crypto.class);
                    return new EntityField(field, crypto.encrypt(), crypto.decrypt());
                }).collect(Collectors.toList());
        entityFieldsCache.put(parameterClass, cryptoFields);
        return cryptoFields;
    }

    private static class EntityField {

        private Field field;
        private boolean encrypt;
        private boolean decrypt;

        EntityField(Field field, boolean encrypt, boolean decrypt) {
            this.field = field;
            this.encrypt = encrypt;
            this.decrypt = decrypt;
        }

        public Field getField() {
            return field;
        }

        public boolean isEncrypt() {
            return encrypt;
        }

        public boolean isDecrypt() {
            return decrypt;
        }
    }

}
