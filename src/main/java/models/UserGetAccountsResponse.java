package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// TODO: has the same structure as DepositMoneyResponse -> Can be optimized?
public class UserGetAccountsResponse extends BaseModel{
    private int id;
    private String accountNumber;
    private float balance;
    private List<Transaction> transactions;
}
