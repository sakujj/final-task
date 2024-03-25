package io.github.sakujj.nms.filter;

import lombok.experimental.UtilityClass;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.springframework.util.CollectionUtils.unmodifiableMultiValueMap;

/**
 * Inspired by {@link org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions#removeRequestParameter}
 */
@UtilityClass
public class EncodeRequestParametersFilter {

    public static Function<ServerRequest, ServerRequest> encodeRequestParameters(Charset charset) {

        return request ->
        {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(request.params());
            Set<Map.Entry<String, List<String>>> queryParamSet = queryParams.entrySet();

            queryParamSet.forEach(entry ->
            {
                List<String> paramValues = entry.getValue();
                IntStream.range(0, paramValues.size())
                        .forEach(i ->
                        {
                            String paramValue = paramValues.get(i);

                            String encodedParamValue = new String(paramValue.getBytes(charset), charset);

                            paramValues.set(i, encodedParamValue);
                        });

            });

            // replace not encoded params in uri
            URI newUri = UriComponentsBuilder.fromUri(request.uri())
                    .replaceQueryParams(unmodifiableMultiValueMap(queryParams)).build().toUri();

            // replace params in request
            return ServerRequest.from(request).params(params -> {
                        params.clear();
                        params.addAll(queryParams);
                    })
                    .uri(newUri).build();
        };
    }
}
