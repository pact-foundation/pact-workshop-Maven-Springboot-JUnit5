# Pact Maven + Springboot + JUnit5 workshop

## Introduction

This workshop is aimed at demonstrating core features and benefits of contract testing with Pact.

Whilst contract testing can be applied retrospectively to systems, we will follow the [consumer driven contracts](https://martinfowler.com/articles/consumerDrivenContracts.html) approach in this workshop - where a new consumer and provider are created in parallel to evolve a service over time, especially where there is some uncertainty with what is to be built.

This workshop should take from 1 to 2 hours, depending on how deep you want to go into each topic.

**Workshop outline**:

- [step 1: **create consumer**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step1#step-1---simple-consumer-calling-provider): Create our consumer before the Provider API even exists
- [step 2: **unit test**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step2#step-2---client-tested-but-integration-fails): Write a unit test for our consumer
- [step 3: **pact test**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step3#step-3---pact-to-the-rescue): Write a Pact test for our consumer
- [step 4: **pact verification**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step4#step-4---verify-the-provider): Verify the consumer pact with the Provider API
- [step 5: **fix consumer**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step5#step-5---back-to-the-client-we-go): Fix the consumer's bad assumptions about the Provider
- [step 6: **pact test**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step6#step-6---consumer-updates-contract-for-missing-products): Write a pact test for `404` (missing User) in consumer
- [step 7: **provider states**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step7#step-7---adding-the-missing-states): Update API to handle `404` case
- [step 8: **pact test**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step8#step-8---authorization): Write a pact test for the `401` case
- [step 9: **pact test**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step9#step-9---implement-authorisation-on-the-provider): Update API to handle `401` case
- [step 10: **request filters**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step10#step-10---request-filters-on-the-provider): Fix the provider to support the `401` case
- [step 11: **pact broker**](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step11#step-11---using-a-pact-broker): Implement a broker workflow for integration with CI/CD

_NOTE: Each step is tied to, and must be run within, a git branch, allowing you to progress through each stage incrementally. For example, to move to step 2 run the following: `git checkout step2`_

## Learning objectives

If running this as a team workshop format, you may want to take a look through the [learning objectives](./LEARNING.md).

## Requirements

- JDK 8 or above
- Maven 3

## Scenario

There are two components in scope for our workshop.

1. Product Catalog website. It provides an interface to query the Product service for product information.
1. Product Service (Provider). Provides useful things about products, such as listing all products and getting the details of an individual product.

## Step 1 - Simple Consumer calling Provider

We need to first create an HTTP client to make the calls to our provider service:

![Simple Consumer](diagrams/workshop_step1.svg)

The Consumer has implemented the product service client which has the following:

- `GET /products` - Retrieve all products
- `GET /products/{id}` - Retrieve a single product by ID

The diagram below highlights the interaction for retrieving a product with ID 10:

![Sequence Diagram](diagrams/workshop_step1_class-sequence-diagram.svg)

You can see the service client interface we created in `consumer/src/main/java/io/pact/workshop/product_catalogue/clients/ProductServiceClient.java`:

```java
@Service
public class ProductServiceClient {
  @Autowired
  private RestTemplate restTemplate;

  @Value("${serviceClients.products.baseUrl}")
  private String baseUrl;

  public ProductServiceResponse fetchProducts() {
    return restTemplate.getForObject(baseUrl + "/products", ProductServiceResponse.class);
  }

  public Product fetchProductById(long id) {
    return restTemplate.getForObject(baseUrl + "/products/" + id, Product.class);
  }
}
```

After forking or cloning the repository, we need to build the app and install the dependencies with `./mvnw verify`.
We can run the app with `java -jar target/product-catalogue-0.0.1-SNAPSHOT.jar`.

Accessing the URL for the app in the browser gives us a 500 error page as the downstream service is not running. 
You will also see an exception in the Springboot console output.

```
 I/O error on GET request for "http://localhost:9000/products": Connection refused
```

*Move on to [step 2](https://github.com/pact-foundation/pact-workshop-Maven-Springboot-JUnit5/tree/step2#step-2---client-tested-but-integration-fails)*

## Step 2 - Client Tested but integration fails

Now let's create a basic test for our API client. We're going to check 2 things:

1. That our client code hits the expected endpoint
1. That the response is marshalled into an object that is usable, with the correct ID

You can see the client interface test we created in `consumer/src/test/java/io/pact/workshop/product_catalogue/clients/ProductServiceClientTest.java`:

```java
  @Test
  void getProductById(@Wiremock WireMockServer server, @WiremockUri String uri) {
      productServiceClient.setBaseUrl(uri);
      server.stubFor(
          get(urlPathEqualTo("/products/100"))
              .willReturn(aResponse()
              .withStatus(200)
              .withBody("{\n" +
                  "            \"id\": 50,\n" +
                  "            \"type\": \"CREDIT_CARD\",\n" +
                  "            \"name\": \"28 Degrees\",\n" +
                  "            \"version\": \"v1\"\n" +
                  "        }\n")
              .withHeader("Content-Type", "application/json"))
      );
    
      Product product = productServiceClient.getProductById(100);
      assertThat(product, is(equalTo(new Product(50L, "28 Degrees", "CREDIT_CARD", "v1"))));
  }
```



![Unit Test With Mocked Response](diagrams/workshop_step2_unit_test.svg)



Let's run this test and see it all pass:

```console
❯ ./mvnw verify
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------< io.pact.workshop:product-catalogue >-----------------
[INFO] Building product-catalogue 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ product-catalogue ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 1 resource
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ product-catalogue ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ product-catalogue ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] skip non existing resourceDirectory /home/ronald/Development/Projects/Pact/pact-workshop-Maven-Springboot-JUnit5/consumer/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ product-catalogue ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /home/ronald/Development/Projects/Pact/pact-workshop-Maven-Springboot-JUnit5/consumer/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ product-catalogue ---
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.pact.workshop.product_catalogue.clients.ProductServiceClientTest
13:37:44.549 [main] DEBUG org.springframework.test.context.BootstrapUtils - Instantiating CacheAwareContextLoaderDelegate from class [org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate]
13:37:44.559 [main] DEBUG org.springframework.test.context.BootstrapUtils - Instantiating BootstrapContext using constructor [public org.springframework.test.context.support.DefaultBootstrapContext(java.lang.Class,org.springframework.test.context.CacheAwareContextLoaderDelegate)]
13:37:44.588 [main] DEBUG org.springframework.test.context.BootstrapUtils - Instantiating TestContextBootstrapper for test class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest] from class [org.springframework.boot.test.context.SpringBootTestContextBootstrapper]
13:37:44.597 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper - Neither @ContextConfiguration nor @ContextHierarchy found for test class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest], using SpringBootContextLoader
13:37:44.600 [main] DEBUG org.springframework.test.context.support.AbstractContextLoader - Did not detect default resource location for test class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest]: class path resource [io/pact/workshop/product_catalogue/clients/ProductServiceClientTest-context.xml] does not exist
13:37:44.600 [main] DEBUG org.springframework.test.context.support.AbstractContextLoader - Did not detect default resource location for test class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest]: class path resource [io/pact/workshop/product_catalogue/clients/ProductServiceClientTestContext.groovy] does not exist
13:37:44.600 [main] INFO org.springframework.test.context.support.AbstractContextLoader - Could not detect default resource locations for test class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest]: no resource found for suffixes {-context.xml, Context.groovy}.
13:37:44.601 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils - Could not detect default configuration classes for test class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest]: ProductServiceClientTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
13:37:44.629 [main] DEBUG org.springframework.test.context.support.ActiveProfilesUtils - Could not find an 'annotation declaring class' for annotation type [org.springframework.test.context.ActiveProfiles] and class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest]
13:37:44.673 [main] DEBUG org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider - Identified candidate component class: file [/home/ronald/Development/Projects/Pact/pact-workshop-Maven-Springboot-JUnit5/consumer/target/classes/io/pact/workshop/product_catalogue/Application.class]
13:37:44.674 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper - Found @SpringBootConfiguration io.pact.workshop.product_catalogue.Application for test class io.pact.workshop.product_catalogue.clients.ProductServiceClientTest
13:37:44.737 [main] DEBUG org.springframework.boot.test.context.SpringBootTestContextBootstrapper - @TestExecutionListeners is not present for class [io.pact.workshop.product_catalogue.clients.ProductServiceClientTest]: using defaults.
13:37:44.737 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper - Loaded default TestExecutionListener class names from location [META-INF/spring.factories]: [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener, org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener, org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener, org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerResetTestExecutionListener, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrintOnlyOnFailureTestExecutionListener, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener, org.springframework.boot.test.autoconfigure.webservices.client.MockWebServiceServerTestExecutionListener, org.springframework.test.context.web.ServletTestExecutionListener, org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener, org.springframework.test.context.event.ApplicationEventsTestExecutionListener, org.springframework.test.context.support.DependencyInjectionTestExecutionListener, org.springframework.test.context.support.DirtiesContextTestExecutionListener, org.springframework.test.context.transaction.TransactionalTestExecutionListener, org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener, org.springframework.test.context.event.EventPublishingTestExecutionListener]
13:37:44.745 [main] DEBUG org.springframework.boot.test.context.SpringBootTestContextBootstrapper - Skipping candidate TestExecutionListener [org.springframework.test.context.transaction.TransactionalTestExecutionListener] due to a missing dependency. Specify custom listener classes or make the default listener classes and their required dependencies available. Offending class: [org/springframework/transaction/TransactionDefinition]
13:37:44.745 [main] DEBUG org.springframework.boot.test.context.SpringBootTestContextBootstrapper - Skipping candidate TestExecutionListener [org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener] due to a missing dependency. Specify custom listener classes or make the default listener classes and their required dependencies available. Offending class: [org/springframework/transaction/interceptor/TransactionAttribute]
13:37:44.745 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper - Using TestExecutionListeners: [org.springframework.test.context.web.ServletTestExecutionListener@55a147cc, org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener@71ba6d4e, org.springframework.test.context.event.ApplicationEventsTestExecutionListener@738dc9b, org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener@3c77d488, org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener@63376bed, org.springframework.test.context.support.DirtiesContextTestExecutionListener@4145bad8, org.springframework.test.context.event.EventPublishingTestExecutionListener@d86a6f, org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener@2892d68, org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener@5ab956d7, org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerResetTestExecutionListener@3646a422, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrintOnlyOnFailureTestExecutionListener@750e2b97, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener@3e27aa33, org.springframework.boot.test.autoconfigure.webservices.client.MockWebServiceServerTestExecutionListener@2e385cce]
13:37:44.748 [main] DEBUG org.springframework.test.context.support.AbstractDirtiesContextTestExecutionListener - Before test class: context [DefaultTestContext@2e8c1c9b testClass = ProductServiceClientTest, testInstance = [null], testMethod = [null], testException = [null], mergedContextConfiguration = [WebMergedContextConfiguration@53fe15ff testClass = ProductServiceClientTest, locations = '{}', classes = '{class io.pact.workshop.product_catalogue.Application}', contextInitializerClasses = '[]', activeProfiles = '{}', propertySourceLocations = '{}', propertySourceProperties = '{org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true}', contextCustomizers = set[org.springframework.boot.test.context.filter.ExcludeFilterContextCustomizer@9353778, org.springframework.boot.test.json.DuplicateJsonObjectContextCustomizerFactory$DuplicateJsonObjectContextCustomizer@1700915, org.springframework.boot.test.mock.mockito.MockitoContextCustomizer@0, org.springframework.boot.test.web.client.TestRestTemplateContextCustomizer@31c88ec8, org.springframework.boot.test.autoconfigure.actuate.metrics.MetricsExportContextCustomizerFactory$DisableMetricExportContextCustomizer@3427b02d, org.springframework.boot.test.autoconfigure.properties.PropertyMappingContextCustomizer@0, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverContextCustomizerFactory$Customizer@6eda5c9, org.springframework.boot.test.context.SpringBootTestArgs@1, org.springframework.boot.test.context.SpringBootTestWebEnvironment@7921b0a2], resourceBasePath = 'src/main/webapp', contextLoader = 'org.springframework.boot.test.context.SpringBootContextLoader', parent = [null]], attributes = map['org.springframework.test.context.web.ServletTestExecutionListener.activateListener' -> true]], class annotated with @DirtiesContext [false] with mode [null].
13:37:44.769 [main] DEBUG org.springframework.test.context.support.TestPropertySourceUtils - Adding inlined properties to environment: {spring.jmx.enabled=false, org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true}

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.4.3)

2021-02-25 13:37:44.957  INFO 25640 --- [           main] i.p.w.p.c.ProductServiceClientTest       : Starting ProductServiceClientTest using Java 1.8.0_265 on ronald-P95xER with PID 25640 (started by ronald in /home/ronald/Development/Projects/Pact/pact-workshop-Maven-Springboot-JUnit5/consumer)
2021-02-25 13:37:44.959  INFO 25640 --- [           main] i.p.w.p.c.ProductServiceClientTest       : No active profile set, falling back to default profiles: default
2021-02-25 13:37:45.755  INFO 25640 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2021-02-25 13:37:45.936  INFO 25640 --- [           main] i.p.w.p.c.ProductServiceClientTest       : Started ProductServiceClientTest in 1.162 seconds (JVM running for 1.748)
2021-02-25 13:37:46.147  INFO 25640 --- [           main] org.eclipse.jetty.util.log               : Logging initialized @1959ms to org.eclipse.jetty.util.log.Slf4jLog
2021-02-25 13:37:46.224  INFO 25640 --- [           main] org.eclipse.jetty.server.Server          : jetty-9.4.36.v20210114; built: 2021-01-14T16:44:28.689Z; git: 238ec6997c7806b055319a6d11f8ae7564adc0de; jvm 1.8.0_265-b01
2021-02-25 13:37:46.234  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Started o.e.j.s.ServletContextHandler@12a14b74{/__admin,null,AVAILABLE}
2021-02-25 13:37:46.235  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Started o.e.j.s.ServletContextHandler@2be95d31{/,null,AVAILABLE}
2021-02-25 13:37:46.265  INFO 25640 --- [           main] o.e.jetty.server.AbstractConnector       : Started NetworkTrafficServerConnector@9b9a327{HTTP/1.1, (http/1.1)}{0.0.0.0:34893}
2021-02-25 13:37:46.265  INFO 25640 --- [           main] org.eclipse.jetty.server.Server          : Started @2078ms
2021-02-25 13:37:46.266  INFO 25640 --- [           main] ru.lanwen.wiremock.ext.WiremockResolver  : Started wiremock server on localhost:34893
2021-02-25 13:37:46.396  INFO 25640 --- [qtp801996095-24] o.e.j.s.handler.ContextHandler.ROOT      : RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.StubRequestHandler. Normalized mapped under returned 'null'
2021-02-25 13:37:46.440  INFO 25640 --- [qtp801996095-24] WireMock                                 : Request received:
127.0.0.1 - GET /products/10

Accept: [application/json, application/*+json]
Connection: [keep-alive]
User-Agent: [Apache-HttpClient/4.5.13 (Java/1.8.0_265)]
Host: [localhost:34893]
Accept-Encoding: [gzip,deflate]



Matched response definition:
{
  "status" : 200,
  "body" : "{\n            \"id\": 10,\n            \"type\": \"CREDIT_CARD\",\n            \"name\": \"28 Degrees\",\n            \"version\": \"v1\"\n        }\n",
  "headers" : {
    "Content-Type" : "application/json"
  }
}

Response:
HTTP/1.1 200
Content-Type: [application/json]
Matched-Stub-Id: [25ea5cf1-9b0c-4e90-9aed-c53bbefd1814]


2021-02-25 13:37:46.465  INFO 25640 --- [           main] ru.lanwen.wiremock.ext.WiremockResolver  : Stopping wiremock server on localhost:34893
2021-02-25 13:37:46.468  INFO 25640 --- [           main] o.e.jetty.server.AbstractConnector       : Stopped NetworkTrafficServerConnector@9b9a327{HTTP/1.1, (http/1.1)}{0.0.0.0:0}
2021-02-25 13:37:46.468  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Stopped o.e.j.s.ServletContextHandler@2be95d31{/,null,STOPPED}
2021-02-25 13:37:46.469  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Stopped o.e.j.s.ServletContextHandler@12a14b74{/__admin,null,STOPPED}
2021-02-25 13:37:46.478  INFO 25640 --- [           main] org.eclipse.jetty.server.Server          : jetty-9.4.36.v20210114; built: 2021-01-14T16:44:28.689Z; git: 238ec6997c7806b055319a6d11f8ae7564adc0de; jvm 1.8.0_265-b01
2021-02-25 13:37:46.479  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Started o.e.j.s.ServletContextHandler@1d8b0500{/__admin,null,AVAILABLE}
2021-02-25 13:37:46.479  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Started o.e.j.s.ServletContextHandler@76544c0a{/,null,AVAILABLE}
2021-02-25 13:37:46.480  INFO 25640 --- [           main] o.e.jetty.server.AbstractConnector       : Started NetworkTrafficServerConnector@49469ffa{HTTP/1.1, (http/1.1)}{0.0.0.0:32859}
2021-02-25 13:37:46.480  INFO 25640 --- [           main] org.eclipse.jetty.server.Server          : Started @2293ms
2021-02-25 13:37:46.480  INFO 25640 --- [           main] ru.lanwen.wiremock.ext.WiremockResolver  : Started wiremock server on localhost:32859
2021-02-25 13:37:46.485  INFO 25640 --- [tp1487884406-33] o.e.j.s.handler.ContextHandler.ROOT      : RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.StubRequestHandler. Normalized mapped under returned 'null'
2021-02-25 13:37:46.487  INFO 25640 --- [tp1487884406-33] WireMock                                 : Request received:
127.0.0.1 - GET /products

Accept: [application/json, application/*+json]
Connection: [keep-alive]
User-Agent: [Apache-HttpClient/4.5.13 (Java/1.8.0_265)]
Host: [localhost:32859]
Accept-Encoding: [gzip,deflate]



Matched response definition:
{
  "status" : 200,
  "body" : "{\n\"products\": [\n            {\n                \"id\": 9,\n                \"type\": \"CREDIT_CARD\",\n                \"name\": \"GEM Visa\",\n                \"version\": \"v2\"\n            },\n            {\n                \"id\": 10,\n                \"type\": \"CREDIT_CARD\",\n                \"name\": \"28 Degrees\",\n                \"version\": \"v1\"\n            }\n        ]\n\n}",
  "headers" : {
    "Content-Type" : "application/json"
  }
}

Response:
HTTP/1.1 200
Content-Type: [application/json]
Matched-Stub-Id: [dcfa90de-b29d-477f-b12e-a56d56500849]


2021-02-25 13:37:46.491  INFO 25640 --- [           main] ru.lanwen.wiremock.ext.WiremockResolver  : Stopping wiremock server on localhost:32859
2021-02-25 13:37:46.491  INFO 25640 --- [           main] o.e.jetty.server.AbstractConnector       : Stopped NetworkTrafficServerConnector@49469ffa{HTTP/1.1, (http/1.1)}{0.0.0.0:0}
2021-02-25 13:37:46.491  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Stopped o.e.j.s.ServletContextHandler@76544c0a{/,null,STOPPED}
2021-02-25 13:37:46.492  INFO 25640 --- [           main] o.e.jetty.server.handler.ContextHandler  : Stopped o.e.j.s.ServletContextHandler@1d8b0500{/__admin,null,STOPPED}
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.952 s - in io.pact.workshop.product_catalogue.clients.ProductServiceClientTest
2021-02-25 13:37:46.510  INFO 25640 --- [extShutdownHook] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'applicationTaskExecutor'
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ product-catalogue ---
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.4.3:repackage (repackage) @ product-catalogue ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.535 s
[INFO] Finished at: 2021-02-25T13:37:47+11:00
[INFO] ------------------------------------------------------------------------
```

If you encounter failing tests after running `./mvnw verify`, make sure that the current branch is `step2`.

Meanwhile, our provider team has started building out their API in parallel. Let's run our website against our provider (you'll need two terminals to do this):


```console
# Terminal 1
❯ 

Provider API listening on port 8080...
```

```console
# Terminal 2
> npm start --prefix consumer

Compiled successfully!

You can now view pact-workshop-js in the browser.

  Local:            http://localhost:3000/
  On Your Network:  http://192.168.20.17:3000/

Note that the development build is not optimized.
To create a production build, use npm run build.
```

You should now see a screen showing 3 different products. There is a `See more!` button which should display detailed product information.

Let's see what happens!

![Failed page](diagrams/workshop_step2_failed_page.png)

Doh! We are getting 404 everytime we try to view detailed product information. On closer inspection, the provider only knows about `/product/{id}` and `/products`.

We need to have a conversation about what the endpoint should be, but first...

*Move on to [step 3](https://github.com/pact-foundation/pact-workshop-js/tree/step3#step-3---pact-to-the-rescue)*
