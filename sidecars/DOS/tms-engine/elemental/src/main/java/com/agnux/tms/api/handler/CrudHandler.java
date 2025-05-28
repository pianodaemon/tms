package com.agnux.tms.api.handler;

public interface CrudHandler<W, R> {

    public abstract R create(W request);

    public abstract R read(W request);

    public abstract R update(W request);

    public abstract R delete(W request);

    public abstract R listPaginated(W request);

}
