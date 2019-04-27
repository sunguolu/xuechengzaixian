package com.xuecheng.bean.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.core.SpringProperties;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachedIntrispectionResultsUtils {

    public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";

    private static final Log logger = LogFactory.getLog(CachedIntrispectionResultsUtils.class);

    private static List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(
            BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());

    /**
     * Map keyed by Class containing CachedIntrospectionResults, strongly held.
     * This variant is being used for cache-safe bean classes.
     */
    static final ConcurrentMap<Class<?>, CachedIntrispectionResultsUtils> strongClassCache =
            new ConcurrentHashMap<>(64);

    /**
     * Map keyed by Class containing CachedIntrospectionResults, softly held.
     * This variant is being used for non-cache-safe bean classes.
     */
    static final ConcurrentMap<Class<?>, CachedIntrispectionResultsUtils> softClassCache =
            new ConcurrentReferenceHashMap<>(64);

    /** The BeanInfo object for the introspected bean class */
    private final BeanInfo beanInfo;
    /** PropertyDescriptor objects keyed by property name String */
    private final Map<String, PropertyDescriptor> propertyDescriptorCache;

    /** TypeDescriptor objects keyed by PropertyDescriptor */
    private final ConcurrentMap<PropertyDescriptor, TypeDescriptor> typeDescriptorCache;

    private static final boolean shouldIntrospectorIgnoreBeaninfoClasses =
            SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);

    /**
     * Set of ClassLoaders that this CachedIntrospectionResults class will always
     * accept classes from, even if the classes do not qualify as cache-safe.
     */
    static final Set<ClassLoader> acceptedClassLoaders =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    BeanInfo getBeanInfo() {
        return this.beanInfo;
    }

    Class<?> getBeanClass() {
        return this.beanInfo.getBeanDescriptor().getBeanClass();
    }




    private CachedIntrispectionResultsUtils(Class<?> beanClass) throws BeansException {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Getting BeanInfo for class [" + beanClass.getName() + "]");
            }
            this.beanInfo = getBeanInfo(beanClass);

            if (logger.isTraceEnabled()) {
                logger.trace("Caching PropertyDescriptors for class [" + beanClass.getName() + "]");
            }
            this.propertyDescriptorCache = new LinkedHashMap<>();

            // This call is slow so we do it once.
            PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (Class.class == beanClass &&
                        ("classLoader".equals(pd.getName()) ||  "protectionDomain".equals(pd.getName()))) {
                    // Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
                    continue;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Found bean property '" + pd.getName() + "'" +
                            (pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") +
                            (pd.getPropertyEditorClass() != null ?
                                    "; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
                }
                pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
                this.propertyDescriptorCache.put(pd.getName(), pd);
            }

            // Explicitly check implemented interfaces for setter/getter methods as well,
            // in particular for Java 8 default methods...
            Class<?> clazz = beanClass;
            while (clazz != null && clazz != Object.class) {
                Class<?>[] ifcs = clazz.getInterfaces();
                for (Class<?> ifc : ifcs) {
                    if (!ClassUtils.isJavaLanguageInterface(ifc)) {
                        for (PropertyDescriptor pd : getBeanInfo(ifc).getPropertyDescriptors()) {
                            if (!this.propertyDescriptorCache.containsKey(pd.getName())) {
                                pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
                                this.propertyDescriptorCache.put(pd.getName(), pd);
                            }
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }

            this.typeDescriptorCache = new ConcurrentReferenceHashMap<>();
        }
        catch (IntrospectionException ex) {
            throw new FatalBeanException("Failed to obtain BeanInfo for class [" + beanClass.getName() + "]", ex);
        }
    }



    static CachedIntrispectionResultsUtils forClass(Class<?> beanClass) throws BeansException {
        CachedIntrispectionResultsUtils results = strongClassCache.get(beanClass);
        if (results != null) {
            return results;
        }
        results = softClassCache.get(beanClass);
        if (results != null) {
            return results;
        }

        results = new CachedIntrispectionResultsUtils(beanClass);
        ConcurrentMap<Class<?>, CachedIntrispectionResultsUtils> classCacheToUse;

        if (ClassUtils.isCacheSafe(beanClass, CachedIntrispectionResultsUtils.class.getClassLoader()) ||
                isClassLoaderAccepted(beanClass.getClassLoader())) {
            classCacheToUse = strongClassCache;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not strongly caching class [" + beanClass.getName() + "] because it is not cache-safe");
            }
            classCacheToUse = softClassCache;
        }

        CachedIntrispectionResultsUtils existing = classCacheToUse.putIfAbsent(beanClass, results);
        return (existing != null ? existing : results);
    }


    private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
        try {
            return new GenericTypeAwarePropertyDescriptorUtils(beanClass, pd.getName(), pd.getReadMethod(),
                    pd.getWriteMethod(), pd.getPropertyEditorClass());
        }
        catch (IntrospectionException ex) {
            throw new FatalBeanException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
        }
    }

    private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
        for (ClassLoader acceptedLoader : acceptedClassLoaders) {
            if (isUnderneathClassLoader(classLoader, acceptedLoader)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given ClassLoader is underneath the given parent,
     * that is, whether the parent is within the candidate's hierarchy.
     * @param candidate the candidate ClassLoader to check
     * @param parent the parent ClassLoader to check for
     */
    private static boolean isUnderneathClassLoader(@Nullable ClassLoader candidate, @Nullable ClassLoader parent) {
        if (candidate == parent) {
            return true;
        }
        if (candidate == null) {
            return false;
        }
        ClassLoader classLoaderToCheck = candidate;
        while (classLoaderToCheck != null) {
            classLoaderToCheck = classLoaderToCheck.getParent();
            if (classLoaderToCheck == parent) {
                return true;
            }
        }
        return false;
    }



    private static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
        for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
            BeanInfo beanInfo = beanInfoFactory.getBeanInfo(beanClass);
            if (beanInfo != null) {
                return beanInfo;
            }
        }
        return (shouldIntrospectorIgnoreBeaninfoClasses ?
                Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) :
                Introspector.getBeanInfo(beanClass));
    }




    PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] pds = new PropertyDescriptor[this.propertyDescriptorCache.size()];
        int i = 0;
        for (PropertyDescriptor pd : this.propertyDescriptorCache.values()) {
            pds[i] = (pd instanceof GenericTypeAwarePropertyDescriptorUtils ? pd :
                    buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
            i++;
        }
        return pds;
    }


    @Nullable
    PropertyDescriptor getPropertyDescriptor(String name) {
        PropertyDescriptor pd = this.propertyDescriptorCache.get(name);
        if (pd == null && StringUtils.hasLength(name)) {
            // Same lenient fallback checking as in Property...
            pd = this.propertyDescriptorCache.get(StringUtils.uncapitalize(name));
            if (pd == null) {
                pd = this.propertyDescriptorCache.get(StringUtils.capitalize(name));
            }
        }
        return (pd == null || pd instanceof GenericTypeAwarePropertyDescriptorUtils ? pd :
                buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
    }

}
