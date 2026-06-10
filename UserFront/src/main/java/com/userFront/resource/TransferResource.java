package com.userFront.resource;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.dto.BetweenTransferRequest;
import com.userFront.dto.ExternalTransferRequest;
import com.userFront.dto.RecipientDto;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RestController
@RequestMapping("/api/transfer")
@PreAuthorize("hasRole('USER')")
public class TransferResource {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/between")
    public ResponseEntity<Void> betweenAccounts(@RequestBody BetweenTransferRequest request, Principal principal)
            throws Exception {
        User user = userService.findByUsername(principal.getName());
        transactionService.betweenAccountsTransfer(request.getTransferFrom(), request.getTransferTo(),
                request.getAmount(), user.getPrimaryAccount(), user.getSavingsAccount());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/recipients")
    public List<RecipientDto> getRecipients(Principal principal) {
        List<Recipient> recipientList = transactionService.findRecipientList(principal);

        return recipientList.stream().map(RecipientDto::from).collect(Collectors.toList());
    }

    @PostMapping("/recipients")
    public ResponseEntity<RecipientDto> createRecipient(@RequestBody RecipientDto request, Principal principal) {
        User user = userService.findByUsername(principal.getName());

        Recipient recipient = new Recipient();
        recipient.setName(request.getName());
        recipient.setEmail(request.getEmail());
        recipient.setPhone(request.getPhone());
        recipient.setAccountNumber(request.getAccountNumber());
        recipient.setDescription(request.getDescription());
        recipient.setUser(user);

        Recipient saved = transactionService.saveRecipient(recipient);

        return ResponseEntity.status(HttpStatus.CREATED).body(RecipientDto.from(saved));
    }

    @PutMapping("/recipients/{name}")
    public RecipientDto updateRecipient(@PathVariable("name") String name, @RequestBody RecipientDto request) {
        Recipient recipient = transactionService.findRecipientByName(name);

        recipient.setName(request.getName());
        recipient.setEmail(request.getEmail());
        recipient.setPhone(request.getPhone());
        recipient.setAccountNumber(request.getAccountNumber());
        recipient.setDescription(request.getDescription());

        Recipient saved = transactionService.saveRecipient(recipient);

        return RecipientDto.from(saved);
    }

    @DeleteMapping("/recipients/{name}")
    @Transactional
    public ResponseEntity<Void> deleteRecipient(@PathVariable("name") String name) {
        transactionService.deleteRecipientByName(name);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/external")
    public ResponseEntity<Void> externalTransfer(@RequestBody ExternalTransferRequest request, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Recipient recipient = transactionService.findRecipientByName(request.getRecipientName());
        transactionService.toSomeoneElseTransfer(recipient, request.getAccountType(), request.getAmount(),
                user.getPrimaryAccount(), user.getSavingsAccount());

        return ResponseEntity.ok().build();
    }
}
