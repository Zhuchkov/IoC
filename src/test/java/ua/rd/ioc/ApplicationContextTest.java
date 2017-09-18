package ua.rd.ioc;

import org.junit.Ignore;
import org.junit.Test;

import ua.rd.ioc.annotation.Benchmark;
import ua.rd.ioc.annotation.MyPostConstruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

public class ApplicationContextTest {

	@Test(expected = NoSuchBeanException.class)
	public void getBeanWithEmptyContext() throws Exception {
		Context context = new ApplicationContext();
		context.getBean("abc");
	}

	@Test
	public void getBeanDefinitionNamesWithEmptyContext() throws Exception {
		// given
		Context context = new ApplicationContext();

		// when
		String[] actual = context.getBeanDefinitionNames();

		// tnen
		String[] expected = {};
		assertArrayEquals(expected, actual);
	}

	@Test
	public void getBeanDefinitionNamesWithOneBeanDefinition() throws Exception {
		String beanName = "FirstBean";
		Class<TestBean> beanType = TestBean.class;
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		String[] actual = context.getBeanDefinitionNames();

		String[] expected = { beanName };
		assertArrayEquals(expected, actual);
	}

	@Test
	public void getBeanDefinitionNamesWithSeveralBeanDefinitions() throws Exception {
		String beanName1 = "FirstBean";
		String beanName2 = "SecondBean";
		Class<TestBean> beanType = TestBean.class;
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName1, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
				put(beanName2, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		String[] actual = context.getBeanDefinitionNames();

		String[] expected = { beanName1, beanName2 };
		assertArrayEquals(expected, actual);
	}

	@Test
	public void getBeanDefinitionNamesWithEmptyBeanDefinition() throws Exception {
		List<String> beanDescriptions = Collections.emptyList();
		Config config = new JavaMapConfig(convertTestListToMap(beanDescriptions));
		Context context = new ApplicationContext(config);

		String[] actual = context.getBeanDefinitionNames();

		String[] expected = {};
		assertArrayEquals(expected, actual);
	}

	@Ignore
	@Test(expected = IllegalArgumentException.class)
	public void getBeanWithOneBeanDefinitionWithoutType() throws Exception {
		String beanName = "FirstBean";
		List<String> beanDescriptions = Arrays.asList(beanName);
		Config config = new JavaMapConfig(convertTestListToMap(beanDescriptions));
		Context context = new ApplicationContext(config);

		Object bean = context.getBean(beanName);
	}
	

	@Test
	public void getBeanWithOneBeanDefinition() throws Exception {
		String beanName = "FirstBean";
		Class<TestBean> beanType = TestBean.class;
		// List<String> beanDescriptions = Arrays.asList(beanName);
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBean bean = (TestBean) context.getBean(beanName);

		assertNotNull(bean);
	}

	@Test
	public void getBeanNotSameInstancesWithSameType() throws Exception {
		String beanName1 = "FirstBean";
		String beanName2 = "SecondBean";
		Class<TestBean> beanType = TestBean.class;
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName1, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
				put(beanName2, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBean bean1 = (TestBean) context.getBean(beanName1);
		TestBean bean2 = (TestBean) context.getBean(beanName2);

		assertNotSame(bean1, bean2);
	}

	@Test
	public void getBeanIsSingleton() throws Exception {
		String beanName = "FirstBean";
		Class<TestBean> beanType = TestBean.class;
		// List<String> beanDescriptions = Arrays.asList(beanName);
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBean bean1 = (TestBean) context.getBean(beanName);
		TestBean bean2 = (TestBean) context.getBean(beanName);

		assertSame(bean1, bean2);
	}

	@Test
	public void getBeanIsPrototype() throws Exception {
		String beanName = "FirstBean";
		Class<TestBean> beanType = TestBean.class;
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
						put("isPrototype", true);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBean bean1 = (TestBean) context.getBean(beanName);
		TestBean bean2 = (TestBean) context.getBean(beanName);

		assertNotSame(bean1, bean2);
	}

	@Test
	public void getBeanWithOneDependedBean() throws Exception {
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put("testBean", new HashMap<String, Object>() {
					{
						put("type", TestBean.class);
						put("isPrototype", false);
					}
				});
				put("testBeanWithConstructor", new HashMap<String, Object>() {
					{
						put("type", TestBeanWithConstructor.class);
						put("isPrototype", false);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBeanWithConstructor bean = (TestBeanWithConstructor) context.getBean("testBeanWithConstructor");

		assertNotNull(bean);
	}
	
	

	@Test
	public void getBeanWithSeveralDependedBeansIsSingleton() throws Exception {
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put("testBean", new HashMap<String, Object>() {
					{
						put("type", TestBean.class);
						put("isPrototype", false);
					}
				});
				put("testBeanWithConstructorTwoParams", new HashMap<String, Object>() {
					{
						put("type", TestBeanWithConstructorTwoParams.class);
						put("isPrototype", false);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBeanWithConstructorTwoParams bean = (TestBeanWithConstructorTwoParams) context
				.getBean("testBeanWithConstructorTwoParams");

		assertNotNull(bean);
		assertSame(bean.testBean1, bean.testBean2);
	}

	@Test
	public void getBeanWithSeveralDependedBeansIsPrototype() throws Exception {
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put("testBean", new HashMap<String, Object>() {
					{
						put("type", TestBean.class);
						put("isPrototype", true);
					}
				});
				put("testBeanWithConstructorTwoParams", new HashMap<String, Object>() {
					{
						put("type", TestBeanWithConstructorTwoParams.class);
						put("isPrototype", false);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBeanWithConstructorTwoParams bean = (TestBeanWithConstructorTwoParams) context
				.getBean("testBeanWithConstructorTwoParams");

		assertNotNull(bean);
		assertNotSame(bean.testBean1, bean.testBean2);
	}
	

	@Test
	public void getBeanCallInitMethod() throws Exception {
		String beanName = "FirstBean";
		Class<TestBean> beanType = TestBean.class;
		// List<String> beanDescriptions = Arrays.asList(beanName);
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBean bean = (TestBean) context.getBean(beanName);
		assertNotNull(bean.initProperty);

	}

	@Test
	public void getBeanCallPostConstructMethod() throws Exception {
		String beanName = "FirstBean";
		Class<TestBean> beanType = TestBean.class;
		// List<String> beanDescriptions = Arrays.asList(beanName);
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		TestBean bean = (TestBean) context.getBean(beanName);
		assertNotNull(bean.postConstructProperty);

	}

	@Test
	public void getBeanCallBenchmarkMethod() throws Exception {
		String beanName = "BenchmarkTestBean";
		Class<BenchmarkTestBean> beanType = BenchmarkTestBean.class;
		// List<String> beanDescriptions = Arrays.asList(beanName);
		Map<String, Map<String, Object>> beanDescriptions = new HashMap<String, Map<String, Object>>() {
			{
				put(beanName, new HashMap<String, Object>() {
					{
						put("type", beanType);
						
					}
				});
				put("benchmarkSecondTestBean", new HashMap<String, Object>() {
					{
						put("type", BenchmarkSecondTestBean.class);
						
					}
				});
			}
		};

		Config config = new JavaMapConfig(beanDescriptions);
		Context context = new ApplicationContext(config);

		ITestBean bean = (ITestBean) context.getBean(beanName);
		PrintStream console = System.out;
		TestPrintStream stream = new TestPrintStream("file.txt");
		System.setOut(stream);
		String result = bean.methodToBenchmark("qwe");
		System.setOut(console);
		assertNotNull(stream.testString);

	}


	private static class TestPrintStream extends PrintStream {
		private String testString = "";

		public TestPrintStream(String fileName) throws FileNotFoundException {
			super(fileName);
		}

		@Override
		public void println(String x) {
			testString += x;
		}
		
		
	}

	private Map<String, Map<String, Object>> convertTestListToMap(List<String> beanDescriptionWithBeanNamesOnly) {
		return beanDescriptionWithBeanNamesOnly.stream()
				.collect(Collectors.toMap(Function.identity(), beanName -> new HashMap<>()));
	}

	static class TestBean {
		public String initProperty;
		public String postConstructProperty;

		public void init() {
			initProperty = "initialized";
		}

		@MyPostConstruct
		public void create() {
			postConstructProperty = "initializedByPostConstruct";
		}

		public String methodToBenchmark(String str) {
			// system.getNanoTime

			// переворот строки

			// system.getNanoTime
			// результат в syso
			return new String();
		}
	}

	static class BenchmarkTestBean implements ITestBean {
		public String initProperty;
		public String postConstructProperty;
		private ITestBean benchmarkSecondTestBean;
		
		public BenchmarkTestBean(BenchmarkSecondTestBean testBean) {
			this.benchmarkSecondTestBean=testBean;
		}
		@Benchmark(enabled = true)
		public String methodToBenchmark(String str) {
			
			char[] chars =str.toCharArray();
			for(int i=0,j=chars.length-1;i<j;i++,j--) {
				char temp = chars[i];
				chars[i]=chars[j];
				chars[j]=temp;
			}
			return new String(chars);
		}
	}
	static class BenchmarkSecondTestBean implements ITestBean {
		public String initProperty;
		public String postConstructProperty;
	
		//@Benchmark(enabled = true)
		public String methodToBenchmark(String str) {
			
			char[] chars =str.toCharArray();
			for(int i=0,j=chars.length-1;i<j;i++,j--) {
				char temp = chars[i];
				chars[i]=chars[j];
				chars[j]=temp;
			}
			return new String(chars);
		}
	}

	public static interface ITestBean {
		public String methodToBenchmark(String str);
	}

	static class TestBeanWithConstructor {
		public TestBeanWithConstructor(TestBean testBean) {
			this.testBean = testBean;
		}

		private final TestBean testBean;
		
		public TestBean getTestBean;
	}

	static class TestBeanWithConstructorTwoParams {
		public TestBeanWithConstructorTwoParams(TestBean testBean1, TestBean testBean2) {
			this.testBean1 = testBean1;
			this.testBean2 = testBean2;
		}

		public final TestBean testBean1;
		public final TestBean testBean2;
	}
	

}