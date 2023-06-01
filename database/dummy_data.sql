SET @name = 'SS_3CMC',
    @time = current_timestamp();
INSERT INTO sf_configuration (name,time_stamp)
       VALUES (@name,@time)
ON DUPLICATE KEY UPDATE
    name = @name,
    time_stamp = @time;

SET @id = 0 ,
    @order_date = current_timestamp(),
    @m_qty = 10,
    @b_qty = 10,
    @g_qty = 10;
INSERT INTO inbound_orders (id,order_date,metal_qty,green_qty,blue_qty)
       VALUES (@id,@order_date,@m_qty,@b_qty,@g_qty)
ON DUPLICATE KEY UPDATE
    id = @id,
    order_date = @order_date,
    metal_qty = @m_qty,
    green_qty = @g_qty,
    blue_qty = @b_qty;


SET @id = 0,
    @status  = 'ARRIVED',
    @sf_dist = 'SS_3CMC',
    @in_order_id = 0;
INSERT INTO part (id,status,fk_sf_configuration,fk_inbound_orders)
       VALUES (@id,@status,@sf_dist,@in_order_id)
ON DUPLICATE KEY UPDATE
    id = @id,
    status = @status,
    fk_sf_configuration = @sf_dist,
    fk_inbound_orders = @in_order_id;
