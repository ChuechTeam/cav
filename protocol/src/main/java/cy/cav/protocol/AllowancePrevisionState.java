package cy.cav.protocol;

public enum AllowancePrevisionState {
    /// The beneficiary doesn't want to have this allowance.
    UNWANTED,
    /// The beneficiary wants this allowance, and an estimate request sent to a calculator is pending.
    PENDING,
    /// The beneficiary wants this allowance, and we've successfully received the calculator's result, which
    /// is currently up to date.
    UP_TO_DATE
}
