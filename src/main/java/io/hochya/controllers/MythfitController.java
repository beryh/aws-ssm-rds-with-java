package io.hochya.controllers;

import io.hochya.models.Mythfit;
import io.hochya.services.MythfitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.hochya.controllers.MythfitController.QueryFilters.GOOD_EVIL;
import static io.hochya.controllers.MythfitController.QueryFilters.LAW_CHAOS;

@RestController
@RequestMapping("/mythfits")
public class MythfitController {
    private final MythfitService mythfitService;

    @Autowired
    private MythfitController(MythfitService mythfitService) {
        this.mythfitService = mythfitService;
    }

    enum QueryFilters {
        GOOD_EVIL("goodevil"),
        LAW_CHAOS("lawchaos");

        private static final Map<String,QueryFilters> ENUM_MAP;
        private String value;

        QueryFilters(String value) {
            this.value = value;
        }

        static {
            Map<String, QueryFilters> map = new ConcurrentHashMap<>();
            for (QueryFilters filter : QueryFilters.values()) {
                map.put(filter.value, filter);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static QueryFilters get(String name) {
            if (name == null) return null;
            return ENUM_MAP.get(name.toLowerCase());
        }
    }

    @GetMapping()
    public List<Mythfit> getMysfits(HttpServletResponse response,
                                    @RequestParam(value = "filter", required = false) String filter,
                                    @RequestParam(value = "value", required = false) String value ) {

        response.addHeader("Access-Control-Allow-Origin", "*");

        if (filter == null)
            return mythfitService.getAllMythfits();

        QueryFilters f = QueryFilters.get(filter);
        if (GOOD_EVIL.equals(f)) {
            return mythfitService.getMythfitsByGoodevil(value);
        } else if (LAW_CHAOS.equals(f)) {
            return mythfitService.getMythfitsByLawchaos(value);
        } else {
            return mythfitService.getAllMythfits();
        }
    }
}
