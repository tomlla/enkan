package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author kawasima
 */
@Middleware(name = "methodOverride", dependencies = {"params"})
public class MethodOverrideMiddleware extends AbstractWebMiddleware {
    private Function<HttpRequest, String> getterFunction;

    public MethodOverrideMiddleware() {
        this("X-HTTP-Method-Override");
    }

    public MethodOverrideMiddleware(String getter) {
        this.getterFunction = createGetter(getter);
    }

    public MethodOverrideMiddleware(Function<HttpRequest, String> getterFunction) {
        this.getterFunction = getterFunction;
    }

    protected Function<HttpRequest, String> createQueryGetter(String key) {
        return req -> req.getParams().get(key);
    }

    /**
     * Create a getter function from headers.
     *
     * TODO Vary header
     *
     * @param str header
     * @return A getter function
     */
    protected Function<HttpRequest, String> createHeaderGetter(String str) {
        String header = str.toLowerCase();
        return req -> Optional.ofNullable(req.getHeaders().get(header)).orElse("");
    }

    protected Function<HttpRequest, String> createGetter(String str) {
        if (str.substring(0, 2).toUpperCase().equals("X-")) {
            return createHeaderGetter(str);
        }
        return createQueryGetter(str);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        String val = getterFunction.apply(request);
        // TODO Check method is valid.
        if (val != null) {
            request.setRequestMethod(val);
        }
        HttpResponse response = castToHttpResponse(chain.next(request));
        return response;
    }
}
