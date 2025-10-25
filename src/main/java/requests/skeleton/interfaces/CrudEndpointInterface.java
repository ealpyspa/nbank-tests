package requests.skeleton.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(long id);
    Object update(BaseModel model); // before: Object update(long id, BaseModel model); -> removed id
    Object delete(long id);
}
