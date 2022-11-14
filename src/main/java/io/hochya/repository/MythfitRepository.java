package io.hochya.repository;

import io.hochya.models.Mythfit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MythfitRepository extends JpaRepository<Mythfit, String> {
    List<Mythfit> getMythfitsByGoodevilIgnoreCase(String goodEvil);
    List<Mythfit> getMythfitsByLawchaosIgnoreCase(String lawChaos);
}
