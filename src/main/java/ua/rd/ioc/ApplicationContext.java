package ua.rd.ioc;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import ua.rd.ioc.annotation.Benchmark;
import ua.rd.ioc.annotation.MyPostConstruct;
import ua.rd.ioc.proxy.BenchmarkInvocationHandler;

public class ApplicationContext implements Context {

    private List<BeanDefinition> beanDefinitions;
    private Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(Config config) {
        beanDefinitions = Arrays.asList(config.beanDefinitions());
        initContext(beanDefinitions);
    }

    private void initContext(List<BeanDefinition> beanDefinitions) {
    	beanDefinitions.forEach(t->getBean(t.getBeanName()));
		
	}

	public ApplicationContext() {
        beanDefinitions = Arrays.asList(Config.EMPTY_BEANDEFINITION);//new BeanDefinition[0];
    }

	public Object getBean(String beanName) {
		BeanDefinition beanDefinition = getBeanDefinitionByName(beanName);

		return Optional
				.ofNullable(beans.get(beanName))
				.orElseGet(() -> {
					Object bean = createNewBean(beanDefinition);
					if (!beanDefinition.isPrototype()) {
						beans.put(beanName, bean);
					}
					return bean;
				});

		// Object bean = beans.get(beanName);
		// if(bean!= null) {
		// return bean;
		// }else {
		// bean = createNewBean(beanDefinition);
		// if (!beanDefinition.isPrototype()) {
		// beans.put(beanName, bean);
		// }
		// return bean;
		// }
		//
	}

    private Object createNewBean(BeanDefinition beanDefinition) {
    	BeanBuilder beanBuilder = new BeanBuilder(beanDefinition);
    	beanBuilder.createNewBeanInstance();
    	beanBuilder.callPostConstructAnnotatedMethod();
    	beanBuilder.callInitMethod();
    	beanBuilder.createBenchmarkProxy();
    	Object bean = beanBuilder.build();
    	
//        Object bean = createNewBeanInstance(beanDefinition);
//        callPostConstructAnnotatedMethod(bean);
//        callInitMethod(bean);
//        bean = createBenchmarkProxy(bean);
        return bean;
    }

	

	private BeanDefinition getBeanDefinitionByName(String beanName) {
        return beanDefinitions.stream()
                .filter(bd -> Objects.equals(bd.getBeanName(), beanName))
                .findAny().orElseThrow(NoSuchBeanException::new);
    }

    

    

   

    public String[] getBeanDefinitionNames() {
        return beanDefinitions.stream()
                .map(BeanDefinition::getBeanName)
                .toArray(String[]::new);
    }

	class BeanBuilder {
		private BeanDefinition beanDefinition;
		private Object bean;

		BeanBuilder(BeanDefinition beanDefinition) {
			this.beanDefinition = beanDefinition;
		}

		public Object build() {
			return bean;
		}

		public void createBenchmarkProxy() {
			Method[] methods = bean.getClass().getMethods();
			for (Method method : methods) {
				if (method.getAnnotation(Benchmark.class) != null) {
					bean = Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(),
							new BenchmarkInvocationHandler(bean));
					break;
				}
			}

		}

		public void callInitMethod() {
			Class<?> type = bean.getClass();
			try {

				Method initMethod = type.getMethod("init");
				initMethod.invoke(bean);
			} catch (NoSuchMethodException e) {
				// Do Nothing
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
			}

		}

		public void callPostConstructAnnotatedMethod() {
			Arrays.stream(bean.getClass().getMethods())
					.filter(method -> method.getAnnotation(MyPostConstruct.class) != null).forEach(method -> {
						try {
							method.invoke(bean);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
					});

		}

		public void createNewBeanInstance() {
			Class<?> type = beanDefinition.getBeanType();
			Constructor<?> constructor = type.getDeclaredConstructors()[0];
			Object newBean = null;
			if (constructor.getParameterCount() == 0) {
				newBean = createBeanWithDefaultConstructor(type);
			} else {
				newBean = createBeanWithConstructorWithParams(type);
			}
			bean = newBean;

		}
		
		 private Object createBeanWithDefaultConstructor(Class<?> type) {
		        Object newBean;
		        try {
		           newBean = type.newInstance();
		        } catch (Exception e) {
		            throw new IllegalArgumentException(e);
		        }
		        return newBean;
		    }

		private Object createBeanWithConstructorWithParams(Class<?> type) {
			Constructor<?> constructor = type.getDeclaredConstructors()[0];
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			Object[] args = Arrays.stream(parameterTypes)
					.map(s -> getBean(Introspector.decapitalize(s.getSimpleName()))).toArray();
			System.out.println(type);
			System.out.println("arg " +args[0]);
			try {
				return constructor.newInstance(args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException(e);
				
			}
		}

	}
}
