package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.requests.*;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actor that manages allowance requests.
 * All request data is stored only in the service.
 */
public class AllowanceRequest extends Actor {
    private static final Logger log = LoggerFactory.getLogger(AllowanceRequest.class);
    
    private final AllocationStore store;
    
    static final Router<AllowanceRequest> router = new Router<AllowanceRequest>()
        .route(CreateAllowanceRequestRequest.class, AllowanceRequest::createRequest)
        .route(UpdateAllowanceRequestRequest.class, AllowanceRequest::updateRequest)
        .route(GetAllowanceRequestRequest.class, AllowanceRequest::getRequest);
    
    public AllowanceRequest(ActorInit init, AllocationStore store) {
        super(init);
        this.store = store;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    CreateAllowanceRequestResponse createRequest(CreateAllowanceRequestRequest request) {
        log.info("Creating request for beneficiary: {}, type: {}", request.beneficiaryId(), request.allowanceType());
        
        cy.cav.service.domain.AllowanceRequest allowanceRequest = new cy.cav.service.domain.AllowanceRequest(
            request.beneficiaryId(), request.allowanceType());
        store.saveRequest(allowanceRequest);
        
        log.info("Request created: {} for beneficiary: {}", allowanceRequest.getId(), request.beneficiaryId());
        
        return new CreateAllowanceRequestResponse(
            allowanceRequest.getId(),
            allowanceRequest.getBeneficiaryId(),
            allowanceRequest.getAllowanceType(),
            allowanceRequest.getRequestDate(),
            allowanceRequest.getStatus()
        );
    }
    
    UpdateAllowanceRequestResponse updateRequest(UpdateAllowanceRequestRequest request) {
        log.debug("Updating request: {}", request.requestId());
        
        return store.findRequestById(request.requestId())
            .map(req -> {
                req.setStatus(request.status());
                req.setAllowanceId(request.allowanceId());
                req.setMonthlyAmount(request.monthlyAmount());
                req.setRejectionReason(request.rejectionReason());
                store.saveRequest(req);
                
                return new UpdateAllowanceRequestResponse(true);
            })
            .orElse(new UpdateAllowanceRequestResponse(false));
    }
    
    GetAllowanceRequestResponse getRequest(GetAllowanceRequestRequest request) {
        log.debug("Getting request: {}", request.requestId());
        
        return store.findRequestById(request.requestId())
            .map(req -> new GetAllowanceRequestResponse(
                req.getId(),
                req.getBeneficiaryId(),
                req.getAllowanceType(),
                req.getRequestDate(),
                req.getStatus(),
                req.getAllowanceId(),
                req.getMonthlyAmount(),
                req.getRejectionReason()
            ))
            .orElseThrow(() -> {
                log.warn("Request not found: {}", request.requestId());
                return new RuntimeException("Request not found");
            });
    }
}

