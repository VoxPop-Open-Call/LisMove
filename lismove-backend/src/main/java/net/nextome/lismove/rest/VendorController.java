package net.nextome.lismove.rest;

import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import io.swagger.annotations.ApiOperation;
import net.nextome.lismove.config.SecurityServiceProperties;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Category;
import net.nextome.lismove.models.Shop;
import net.nextome.lismove.models.User;
import net.nextome.lismove.models.Vendor;
import net.nextome.lismove.rest.dto.ShopDto;
import net.nextome.lismove.rest.dto.VendorDto;
import net.nextome.lismove.rest.mappers.VendorMapper;
import net.nextome.lismove.services.FirebaseAuthService;
import net.nextome.lismove.services.ShopService;
import net.nextome.lismove.services.UserService;
import net.nextome.lismove.services.VendorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("vendors")
public class VendorController {

    @Autowired
    private UserService userService;
    @Autowired
    private VendorService vendorService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private SecurityServiceProperties serviceProperties;
    @Autowired
    private FirebaseAuthService firebaseAuthService;
    @Autowired
    private VendorMapper vendorMapper;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @PostMapping
    public VendorDto create(@RequestBody VendorDto dto, @ApiIgnore  HttpServletRequest request){
        //---controllo token---
        String token = request.getHeader(serviceProperties.getJwt_header());
        firebaseAuthService.checkToken(token, dto.getUid());

        return vendorService.create(dto);
    }

    @ApiOperation(value = "update", notes = "Aggiorna i dati dell’utente e del vendor associato identificato dall'uid spedificato.")
    @PutMapping("{uid}")
    public VendorDto update(@PathVariable("uid") String uid, @RequestBody VendorDto dto){
        User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
        Vendor vendor = vendorService.findByUserUid(user.getUid()).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));

        return vendorService.update(user,vendor,dto);
    }

    @GetMapping("{uid}")
    public VendorDto get(@PathVariable("uid") String uid) {
        userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
        Vendor vendor = vendorService.findByUserUid(uid).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));
        return vendorMapper.vendorToDto(vendor);
    }

    @GetMapping()
    public List<VendorDto> get() {
        return vendorMapper.vendorToDto(vendorService.findAll());
    }

    @GetMapping("{uid}/shops")
    public List<ShopDto> getShops(@PathVariable("uid") String uid) {
        Vendor vendor = vendorService.findByUserUid(uid).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));
        return vendorMapper.shopToDto(vendor.getShops());
    }


    @PostMapping("{uid}/shops")
    public ShopDto createShop(@PathVariable("uid") String uid, @RequestBody ShopDto dto) throws IOException, InterruptedException, ApiException {
        Vendor vendor = vendorService.findByUserUid(uid).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));
        return shopService.create(vendor,dto);
    }

    @PutMapping("{uid}/shops/{id}")
    public ShopDto updateShop(@PathVariable("uid") String uid,@PathVariable("id") Long idShop, @RequestBody ShopDto dto) throws IOException, InterruptedException, ApiException {
        vendorService.findByUserUid(uid).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));
        Shop shop = shopService.findById(idShop).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        return shopService.update(shop,dto);
    }

    @DeleteMapping("{uid}/shops/{id}")
    public ShopDto deleteShop(@PathVariable("uid") String uid,@PathVariable("id") Long idShop) {
        vendorService.findByUserUid(uid).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));
        Shop shop = shopService.findById(idShop).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        return shopService.delete(shop);
    }

    @GetMapping("{uid}/shops/{id}")
    public ShopDto getShop(@PathVariable("uid") String uid, @PathVariable("id") Long idShop) {
        Vendor vendor = vendorService.findByUserUid(uid).orElseThrow(() -> new LismoveException("Vendor not found", HttpStatus.NOT_FOUND));
        Shop shop = shopService.findById(idShop).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        ShopDto dto = vendorMapper.shopToDto(shop);
        return dto;
    }

    @GetMapping("categories")
    public List<Category> getCategories() {
        return vendorService.getCategories();
    }

    @PostMapping("coordinates")
    public LatLng getCoordinates(@RequestBody String address){
        if(address.isEmpty())
            return new LatLng();
        try {
            return vendorService.getCoordinates(address);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw  new LismoveException("Address not found", HttpStatus.NOT_FOUND);
        }
    }
}
