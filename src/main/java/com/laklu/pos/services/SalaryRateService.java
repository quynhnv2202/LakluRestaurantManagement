package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.NewSalaryRate;
import com.laklu.pos.dataObjects.request.NewUser;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.entities.User;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.SalaryRateMapper;
import com.laklu.pos.repositories.SalaryRateRepository;
import com.laklu.pos.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SalaryRateService {

    private final SalaryRateRepository salaryRateRepository;
    private final SalaryRateMapper salaryRateMapper;
    private final UserRepository userRepository;

    public List<SalaryRate> getAll() {
        return salaryRateRepository.findAll();
    }

    public SalaryRate createSalaryRate(NewSalaryRate newSalaryRate) {
        SalaryRate salaryRate = salaryRateMapper.toEntity(newSalaryRate);
        return salaryRateRepository.save(salaryRate);
    }

    public SalaryRate updateSalaryRate(Integer id, NewSalaryRate newSalaryRate) {
        SalaryRate existingSalaryRate = findOrFail(id);
        salaryRateMapper.updateEntityFromDto(newSalaryRate, existingSalaryRate);
        return salaryRateRepository.save(existingSalaryRate);
    }

    public void deleteSalaryRate(SalaryRate salaryRate) {
        salaryRateRepository.delete(salaryRate);
    }

    public Optional<SalaryRate> findSalaryRateById(Integer id) {
        return salaryRateRepository.findById(id);
    }

    public SalaryRate findOrFail(Integer id) {
        return this.findSalaryRateById(id).orElseThrow(NotFoundException::new);
    }

    public Optional<SalaryRate> findSalaryRateByName(String name) {
        return salaryRateRepository.findByLevelName(name);
    }

    public boolean isSalaryRateInUse(SalaryRate salaryRate) {
        return userRepository.existsBySalaryRate(salaryRate);
    }
}
