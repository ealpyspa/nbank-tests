package api.requests.steps;

import api.dao.AccountDao;
import api.dao.UserDao;
import api.database.Condition;
import api.database.DBRequest;
import common.helper.StepLogger;

public class DataBaseSteps {
    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        return StepLogger.log("Get account from database by account number: " + accountNumber, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(DBRequest.Table.ACCOUNTS)
                    .where(Condition.equalTo("account_number", accountNumber))
                    .extractAs(AccountDao.class);
        });
    }

    public static UserDao getUserById(long id) {
        return StepLogger.log("Get user from database by id: " + id, () -> {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table(DBRequest.Table.CUSTOMERS)
                    .where(Condition.equalTo("id", id))
                    .extractAs(UserDao.class);
        });
    }
}
