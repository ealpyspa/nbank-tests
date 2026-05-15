package api.dao.comparison;

import api.models.BaseModel;
import org.assertj.core.api.AbstractAssert;

public class DaoModelAssertions {
    private static final DaoComparator DAO_COMPARATOR = new DaoComparator();

    public static DaoModelAssert assertThat(BaseModel apiModel, Object daoModel) {
        return new DaoModelAssert(apiModel, daoModel);
    }

    public static class DaoModelAssert extends AbstractAssert<DaoModelAssert, BaseModel> {
        private final BaseModel apiModel;
        private final Object daoModel;

        public DaoModelAssert(BaseModel apiModel, Object daoModel) {
            super(apiModel, DaoModelAssert.class);
            this.apiModel = apiModel;
            this.daoModel = daoModel;
        }

        public DaoModelAssert match() {
            if (apiModel == null) {
                failWithMessage("API model should not be null");
            }
            if (daoModel == null) {
                failWithMessage("DAO model should not be null");
            }

            try {
                DAO_COMPARATOR.compare(apiModel, daoModel);
            } catch (AssertionError e) {
                failWithMessage(e.getMessage());
            } catch (RuntimeException e) {
                failWithMessage("DAO comparison setup failed: %s", e.getMessage());
            }

            return this;
        }
    }
}
