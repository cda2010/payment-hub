package com.m2i.pti.service;

import com.m2i.pti.domain.BlackList;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.enumeration.ActionBlackList;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.reporting.dto.ListeClientDTO;
import com.m2i.pti.repository.BlackListRepository;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.service.dto.BlackListDTO;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.mapper.BlackListMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service Implementation for managing BlackList.
 */
@Service
@Transactional
public class BlackListService {

    private final Logger log = LoggerFactory.getLogger(BlackListService.class);

    private final BlackListRepository blackListRepository;

    private final BlackListMapper blackListMapper;

    private final ClientService clientService;

    private final ClientRepository clientRepository;

    public BlackListService(BlackListRepository blackListRepository, BlackListMapper blackListMapper, ClientService clientService, ClientRepository clientRepository) {
        this.blackListRepository = blackListRepository;
        this.blackListMapper = blackListMapper;
        this.clientService = clientService;
        this.clientRepository = clientRepository;
    }

    /**
     * Save a blackList.
     *
     * @param blackListDTO the entity to save
     * @return the persisted entity
     */
    public BlackListDTO save(BlackListDTO blackListDTO) {
        log.debug("Request to save BlackList : {}", blackListDTO);
        BlackList blackList = blackListMapper.toEntity(blackListDTO);
        blackList.setSupprime(Boolean.FALSE);
        blackList = blackListRepository.save(blackList);
        return blackListMapper.toDto(blackList);
    }

    /**
     *  Get all the blackLists.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BlackListDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BlackLists");
        return blackListRepository.findAllBySupprimeFalse(pageable)
            .map(blackListMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BlackListDTO> findAllWithCriteria(Pageable pageable,String telephone,String nomClient,String prenomClient,String actionBlackList) {
        log.debug("Request to get all BlackLists");
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalClient = 0;

        Pageable pageable1 = new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }

            @Override
            public int getPageSize() {
                return 0;
            }

            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Pageable withPage(int pageNumber) {
                return this;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public Pageable next() {
                return null;
            }

            @Override
            public Pageable previousOrFirst() {
                return null;
            }

            @Override
            public Pageable first() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
        totalClient=blackListRepository.countWithCriteria(telephone,actionBlackList,nomClient,prenomClient);
        List<BlackListDTO> list= blackListRepository.findAllWithCriteria(telephone,actionBlackList,nomClient,prenomClient,taille,page);
        Page<BlackListDTO> retour = new PageImpl<>(list, pageable1, totalClient);
        return retour;
    }

    /**
     *  Get one blackList by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public BlackListDTO findOne(Long id) {
        log.debug("Request to get BlackList : {}", id);
        BlackList blackList = blackListRepository.findById(id).orElse(null);
        return blackListMapper.toDto(blackList);
    }

    /**
     *  Delete the  blackList by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete BlackList : {}", id);
        BlackList blackList=blackListRepository.findById(id).orElse(null);
        blackList.setSupprime(true);
        blackListRepository.save(blackList);
    }

    @Transactional(readOnly = true)
    public List<Client> findByCode(String id) {
        log.debug("Request to get all BlackLists");
        return blackListRepository.findByCode(id);

    }

    @Transactional (readOnly = true)
    public List<Client> queryAll(){
        return blackListRepository.queryAll();
    }

    @Transactional(readOnly = true)
    public List<Client> findByTel(String id) {
        log.debug("Request to get all BlackLists ");
        return blackListRepository.findByTel(id);
    }

    @Transactional(readOnly = true)
    public List<Client> findByCodeAndTel(String code, String tel) {
        log.debug("Request to get all BlackLists ");
        return blackListRepository.findByCodeAndTel(code,tel);
    }
    @Transactional(readOnly = true)
    public ModelAndView genererListBlacker(ApplicationContext appContext, BlackListDTO blackListDTO)
    {
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        view.setUrl("classpath:/report/test.jrxml");
        view.setApplicationContext(appContext);
        return new ModelAndView(view,params);
    }

    Integer countByClientAndActionBlackList(Client clem, ActionBlackList actionBlackList) {
        if(clem!=null&&clem.getId()!=null&&actionBlackList!=null){
            return blackListRepository.countByClientAndActionBlackList(clem,actionBlackList.FOLLOW );
        }
        return 0;
    }

    @Transactional(readOnly = true)
    public List<ListeClientDTO> findClientByCodeAndTelephone(String code,String telephone){
        List<ListeClientDTO> xListe =new ArrayList<>();
        int max;
        int nbrPage;
        max= clientRepository.countClientPhysiqueByCriteria(code, telephone, null, null, null,null,null);
        nbrPage = (max/1000);
        for (int j=0; j<=nbrPage;j++) {
            Pageable page = PageRequest.of(j, 1000, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            Long taille = new Long(page.getPageSize());
            Long pag = new Long(page.getPageSize() * page.getPageNumber());
            xListe.addAll(clientService.findByTypePersonneReportOptimise(TypePersonne.PHYSIQUE,code,telephone,null,null,null,null, null,null,null, taille,pag));
        }
        return xListe;
    }
}
