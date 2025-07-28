package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Report;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.repository.ReportRepository;
import github.oldLab.oldLab.repository.ReviewRepository;
import github.oldLab.oldLab.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl {

    private final ReportRepository repository;
    private final PersonRepository personRepository;
    private final ShopRepository shopRepository;
    private final ReviewRepository reviewRepository;

}
