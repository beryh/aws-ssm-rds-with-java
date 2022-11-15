package io.hochya.services;

import io.hochya.models.Mythfit;
import io.hochya.repository.MythfitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MythfitService {
    final MythfitRepository mythfitRepository;

    @Autowired
    private MythfitService(MythfitRepository mythfitRepository) {
        this.mythfitRepository = mythfitRepository;
    }

    public List<Mythfit> getAllMythfits() {
        return mythfitRepository.findAll();
    }

    public List<Mythfit> getMythfitsByGoodevil(String goodevil) {
        return mythfitRepository.getMythfitsByGoodevilIgnoreCase(goodevil);
    }

    public List<Mythfit> getMythfitsByLawchaos(String lawChaos) {
        return mythfitRepository.getMythfitsByLawchaosIgnoreCase(lawChaos);
    }
}
