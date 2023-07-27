package net.nextome.lismove.services;

import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.rest.mappers.CouponMapper;
import net.nextome.lismove.rest.mappers.VendorMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class VendorService extends UtilitiesService {
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private VendorMapper vendorMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private GoogleMapsService gmapsService;
    @Autowired
    private ArticleRepository articleRepository;

    public VendorDto create(VendorDto dto){
        //----creazione utente----
        User user = userService.createVendor(dto);
        //----creazione vendor----
        Vendor vendor = vendorMapper.dtoToVendor(dto);
        vendor.setUser(user);
        vendor = vendorRepository.save(vendor);
        return vendorMapper.vendorToDto(vendor);
    }

    public VendorDto update(User dbUser,Vendor dbVendor, VendorDto dto) {
        //--user
        User upd = vendorMapper.dtoToUser(dto);
        userService.update(upd, null, dbUser, "uid", "userType", "homeAddress", "workAddress", "homeWorkPath");

        //---vendor
        Vendor newVendor = vendorMapper.dtoToVendor(dto);
        notNullBeanCopy(newVendor, dbVendor);
        vendorRepository.save(dbVendor);

        return vendorMapper.vendorToDto(dbVendor);
    }

    public Optional<Vendor> findByUserUid(String uid) {
        return vendorRepository.findByUserUid(uid);
    }

    public Optional<Category> findCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getCategories() {
        LinkedList<Category> list = new LinkedList<>();
        categoryRepository.findAll().forEach(list::add);
        return list;
    }

    public LatLng getCoordinates(String address) throws IOException, InterruptedException, ApiException {
        return gmapsService.generateLatLng(address);
    }

    public List<Vendor> findAll() {
        List<Vendor> vendors = new LinkedList<>();
        vendorRepository.findAll().forEach(vendors::add);
        return vendors;
    }
}
