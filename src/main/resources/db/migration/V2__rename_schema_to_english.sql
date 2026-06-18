-- Rename the existing Spanish physical schema without recreating tables or data.
ALTER TABLE usuarios RENAME TO users;
ALTER TABLE usuario_roles RENAME TO user_roles;
ALTER TABLE ciudades RENAME TO cities;
ALTER TABLE ubicaciones RENAME TO locations;
ALTER TABLE categorias_evento RENAME TO categories;
ALTER TABLE especialidades RENAME TO specialties;
ALTER TABLE perfil_clientes RENAME TO customer_profiles;
ALTER TABLE perfil_profesionales RENAME TO specialist_profiles;
ALTER TABLE redes_profesional RENAME TO professional_social_links;
ALTER TABLE imagenes_profesional RENAME TO professional_images;
ALTER TABLE profesional_especialidades RENAME TO professional_specialties;
ALTER TABLE eventos RENAME TO events;
ALTER TABLE evento_categorias RENAME TO event_categories;
ALTER TABLE evento_profesionales RENAME TO event_professionals;
ALTER TABLE imagenes_evento RENAME TO event_images;
ALTER TABLE ocurrencias_evento RENAME TO event_occurrences;
ALTER TABLE servicios_1a1 RENAME TO one_to_one_services;
ALTER TABLE disponibilidad_profesional RENAME TO professional_availability;
ALTER TABLE bloqueos_agenda RENAME TO schedule_blocks;
ALTER TABLE ordenes_compra RENAME TO purchase_orders;
ALTER TABLE orden_items RENAME TO purchase_order_items;
ALTER TABLE metodos_pago_cliente RENAME TO customer_payment_methods;
ALTER TABLE pagos RENAME TO payments;
ALTER TABLE reembolsos RENAME TO refunds;
ALTER TABLE recibos_digitales RENAME TO digital_receipts;
ALTER TABLE reservas_evento RENAME TO event_bookings;
ALTER TABLE asistentes_evento RENAME TO event_attendees;
ALTER TABLE reservas_sesion RENAME TO session_bookings;
ALTER TABLE historial_estados_reserva RENAME TO booking_status_history;
ALTER TABLE favoritos RENAME TO favorites;
ALTER TABLE secciones_inicio RENAME TO home_sections;
ALTER TABLE auditoria RENAME TO audit_logs;

ALTER TABLE roles RENAME COLUMN codigo TO name;
ALTER TABLE roles RENAME COLUMN nombre TO display_name;
ALTER TABLE roles RENAME COLUMN descripcion TO description;
ALTER TABLE roles RENAME COLUMN activo TO active;
ALTER TABLE users RENAME COLUMN estado TO status;
ALTER TABLE users RENAME COLUMN ultimo_acceso_at TO last_login_at;
ALTER TABLE user_roles RENAME COLUMN usuario_id TO user_id;
ALTER TABLE user_roles RENAME COLUMN rol_id TO role_id;
ALTER TABLE refresh_tokens RENAME COLUMN usuario_id TO user_id;
ALTER TABLE cities RENAME COLUMN nombre TO name;
ALTER TABLE cities RENAME COLUMN provincia TO province;
ALTER TABLE cities RENAME COLUMN pais_codigo TO country_code;
ALTER TABLE cities RENAME COLUMN activo TO active;
ALTER TABLE locations RENAME COLUMN nombre TO name;
ALTER TABLE locations RENAME COLUMN direccion TO address;
ALTER TABLE locations RENAME COLUMN ciudad_id TO city_id;
ALTER TABLE locations RENAME COLUMN latitud TO latitude;
ALTER TABLE locations RENAME COLUMN longitud TO longitude;
ALTER TABLE locations RENAME COLUMN indicaciones TO reference;
ALTER TABLE locations RENAME COLUMN activo TO active;
ALTER TABLE categories RENAME COLUMN nombre TO name;
ALTER TABLE categories RENAME COLUMN descripcion TO description;
ALTER TABLE categories RENAME COLUMN imagen_url TO image_url;
ALTER TABLE categories RENAME COLUMN activo TO active;
ALTER TABLE specialties RENAME COLUMN nombre TO name;
ALTER TABLE specialties RENAME COLUMN descripcion TO description;
ALTER TABLE specialties RENAME COLUMN activo TO active;
ALTER TABLE customer_profiles RENAME COLUMN usuario_id TO user_id;
ALTER TABLE customer_profiles RENAME COLUMN nombres TO first_names;
ALTER TABLE customer_profiles RENAME COLUMN apellidos TO last_names;
ALTER TABLE customer_profiles RENAME COLUMN telefono TO phone;
ALTER TABLE customer_profiles RENAME COLUMN ciudad_preferida_id TO preferred_city_id;
ALTER TABLE customer_profiles RENAME COLUMN fecha_nacimiento TO birth_date;
ALTER TABLE specialist_profiles RENAME COLUMN usuario_id TO user_id;
ALTER TABLE specialist_profiles RENAME COLUMN nombre_publico TO public_name;
ALTER TABLE specialist_profiles RENAME COLUMN biografia TO biography;
ALTER TABLE specialist_profiles RENAME COLUMN foto_url TO photo_url;
ALTER TABLE specialist_profiles RENAME COLUMN telefono_whatsapp TO whatsapp_phone;
ALTER TABLE specialist_profiles RENAME COLUMN email_publico TO public_email;
ALTER TABLE specialist_profiles RENAME COLUMN sitio_web TO website;
ALTER TABLE specialist_profiles RENAME COLUMN estado_aprobacion TO approval_status;
ALTER TABLE specialist_profiles RENAME COLUMN estado_publicacion TO publication_status;
ALTER TABLE specialist_profiles RENAME COLUMN aprobado_por TO approved_by;
ALTER TABLE specialist_profiles RENAME COLUMN aprobado_at TO approved_at;
ALTER TABLE specialist_profiles RENAME COLUMN motivo_rechazo TO rejection_reason;
ALTER TABLE professional_social_links RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE professional_social_links RENAME COLUMN tipo TO type;
ALTER TABLE professional_social_links RENAME COLUMN orden TO sort_order;
ALTER TABLE professional_images RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE professional_images RENAME COLUMN texto_alternativo TO alternative_text;
ALTER TABLE professional_images RENAME COLUMN es_portada TO cover;
ALTER TABLE professional_images RENAME COLUMN formato TO format;
ALTER TABLE professional_images RENAME COLUMN peso_bytes TO size_bytes;
ALTER TABLE professional_images RENAME COLUMN orden TO sort_order;
ALTER TABLE professional_specialties RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE professional_specialties RENAME COLUMN especialidad_id TO specialty_id;
ALTER TABLE events RENAME COLUMN titulo TO title;
ALTER TABLE events RENAME COLUMN resumen TO summary;
ALTER TABLE events RENAME COLUMN descripcion TO description;
ALTER TABLE events RENAME COLUMN modalidad TO modality;
ALTER TABLE events RENAME COLUMN tipo_reserva TO reservation_type;
ALTER TABLE events RENAME COLUMN url_reserva_externa TO external_reservation_url;
ALTER TABLE events RENAME COLUMN precio_desde TO starting_price;
ALTER TABLE events RENAME COLUMN moneda TO currency;
ALTER TABLE events RENAME COLUMN edad_minima TO minimum_age;
ALTER TABLE events RENAME COLUMN estado TO status;
ALTER TABLE events RENAME COLUMN destacado TO featured;
ALTER TABLE events RENAME COLUMN creado_por TO created_by;
ALTER TABLE events RENAME COLUMN aprobado_por TO approved_by;
ALTER TABLE events RENAME COLUMN aprobado_at TO approved_at;
ALTER TABLE event_categories RENAME COLUMN evento_id TO event_id;
ALTER TABLE event_categories RENAME COLUMN categoria_id TO category_id;
ALTER TABLE event_professionals RENAME COLUMN evento_id TO event_id;
ALTER TABLE event_professionals RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE event_professionals RENAME COLUMN rol_en_evento TO role;
ALTER TABLE event_images RENAME COLUMN evento_id TO event_id;
ALTER TABLE event_images RENAME COLUMN texto_alternativo TO alternative_text;
ALTER TABLE event_images RENAME COLUMN es_portada TO cover;
ALTER TABLE event_images RENAME COLUMN formato TO format;
ALTER TABLE event_images RENAME COLUMN peso_bytes TO size_bytes;
ALTER TABLE event_images RENAME COLUMN orden TO sort_order;
ALTER TABLE event_occurrences RENAME COLUMN evento_id TO event_id;
ALTER TABLE event_occurrences RENAME COLUMN inicio_at TO starts_at;
ALTER TABLE event_occurrences RENAME COLUMN fin_at TO ends_at;
ALTER TABLE event_occurrences RENAME COLUMN ubicacion_id TO location_id;
ALTER TABLE event_occurrences RENAME COLUMN capacidad TO capacity;
ALTER TABLE event_occurrences RENAME COLUMN cupos_reservados TO reserved_spots;
ALTER TABLE event_occurrences RENAME COLUMN estado TO status;
ALTER TABLE event_occurrences RENAME COLUMN url_virtual TO virtual_url;
ALTER TABLE one_to_one_services RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE one_to_one_services RENAME COLUMN titulo TO title;
ALTER TABLE one_to_one_services RENAME COLUMN descripcion TO description;
ALTER TABLE one_to_one_services RENAME COLUMN duracion_minutos TO duration_minutes;
ALTER TABLE one_to_one_services RENAME COLUMN modalidad TO modality;
ALTER TABLE one_to_one_services RENAME COLUMN ubicacion_id TO location_id;
ALTER TABLE one_to_one_services RENAME COLUMN precio TO price;
ALTER TABLE one_to_one_services RENAME COLUMN moneda TO currency;
ALTER TABLE one_to_one_services RENAME COLUMN estado TO status;
ALTER TABLE one_to_one_services RENAME COLUMN aprobado_por TO approved_by;
ALTER TABLE one_to_one_services RENAME COLUMN aprobado_at TO approved_at;
ALTER TABLE professional_availability RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE professional_availability RENAME COLUMN dia_semana TO day_of_week;
ALTER TABLE professional_availability RENAME COLUMN hora_inicio TO start_time;
ALTER TABLE professional_availability RENAME COLUMN hora_fin TO end_time;
ALTER TABLE professional_availability RENAME COLUMN ubicacion_id TO location_id;
ALTER TABLE professional_availability RENAME COLUMN vigente_desde TO valid_from;
ALTER TABLE professional_availability RENAME COLUMN vigente_hasta TO valid_until;
ALTER TABLE professional_availability RENAME COLUMN activo TO active;
ALTER TABLE schedule_blocks RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE schedule_blocks RENAME COLUMN inicio_at TO starts_at;
ALTER TABLE schedule_blocks RENAME COLUMN fin_at TO ends_at;
ALTER TABLE schedule_blocks RENAME COLUMN motivo TO reason;
ALTER TABLE purchase_orders RENAME COLUMN codigo TO code;
ALTER TABLE purchase_orders RENAME COLUMN cliente_id TO customer_id;
ALTER TABLE purchase_orders RENAME COLUMN descuento_total TO total_discount;
ALTER TABLE purchase_orders RENAME COLUMN importe_total TO total_amount;
ALTER TABLE purchase_orders RENAME COLUMN moneda TO currency;
ALTER TABLE purchase_orders RENAME COLUMN estado TO status;
ALTER TABLE purchase_orders RENAME COLUMN expira_at TO expires_at;
ALTER TABLE purchase_orders RENAME COLUMN pagada_at TO paid_at;
ALTER TABLE purchase_order_items RENAME COLUMN orden_id TO order_id;
ALTER TABLE purchase_order_items RENAME COLUMN tipo_item TO item_type;
ALTER TABLE purchase_order_items RENAME COLUMN referencia_id TO reference_id;
ALTER TABLE purchase_order_items RENAME COLUMN descripcion TO description;
ALTER TABLE purchase_order_items RENAME COLUMN cantidad TO quantity;
ALTER TABLE purchase_order_items RENAME COLUMN precio_unitario TO unit_price;
ALTER TABLE purchase_order_items RENAME COLUMN importe_total TO total_amount;
ALTER TABLE customer_payment_methods RENAME COLUMN cliente_id TO customer_id;
ALTER TABLE customer_payment_methods RENAME COLUMN proveedor TO provider;
ALTER TABLE customer_payment_methods RENAME COLUMN token_encriptado TO encrypted_token;
ALTER TABLE customer_payment_methods RENAME COLUMN marca TO brand;
ALTER TABLE customer_payment_methods RENAME COLUMN predeterminado TO default_method;
ALTER TABLE payments RENAME COLUMN orden_id TO order_id;
ALTER TABLE payments RENAME COLUMN metodo_pago_cliente_id TO customer_payment_method_id;
ALTER TABLE payments RENAME COLUMN proveedor TO provider;
ALTER TABLE payments RENAME COLUMN referencia_externa TO external_reference;
ALTER TABLE payments RENAME COLUMN importe TO amount;
ALTER TABLE payments RENAME COLUMN moneda TO currency;
ALTER TABLE payments RENAME COLUMN estado TO status;
ALTER TABLE payments RENAME COLUMN mensaje_error TO error_message;
ALTER TABLE payments RENAME COLUMN procesado_at TO processed_at;
ALTER TABLE refunds RENAME COLUMN pago_id TO payment_id;
ALTER TABLE refunds RENAME COLUMN importe TO amount;
ALTER TABLE refunds RENAME COLUMN motivo TO reason;
ALTER TABLE refunds RENAME COLUMN referencia_externa TO external_reference;
ALTER TABLE refunds RENAME COLUMN estado TO status;
ALTER TABLE digital_receipts RENAME COLUMN pago_id TO payment_id;
ALTER TABLE digital_receipts RENAME COLUMN numero TO number;
ALTER TABLE digital_receipts RENAME COLUMN cliente_nombre TO customer_name;
ALTER TABLE digital_receipts RENAME COLUMN cliente_email TO customer_email;
ALTER TABLE digital_receipts RENAME COLUMN descripcion_compra TO purchase_description;
ALTER TABLE digital_receipts RENAME COLUMN importe TO amount;
ALTER TABLE digital_receipts RENAME COLUMN moneda TO currency;
ALTER TABLE digital_receipts RENAME COLUMN estado TO status;
ALTER TABLE digital_receipts RENAME COLUMN emitido_at TO issued_at;
ALTER TABLE digital_receipts RENAME COLUMN anulado_at TO annulled_at;
ALTER TABLE event_bookings RENAME COLUMN codigo TO code;
ALTER TABLE event_bookings RENAME COLUMN orden_item_id TO order_item_id;
ALTER TABLE event_bookings RENAME COLUMN cliente_id TO customer_id;
ALTER TABLE event_bookings RENAME COLUMN ocurrencia_id TO occurrence_id;
ALTER TABLE event_bookings RENAME COLUMN cantidad TO quantity;
ALTER TABLE event_bookings RENAME COLUMN precio_unitario TO unit_price;
ALTER TABLE event_bookings RENAME COLUMN importe_total TO total_amount;
ALTER TABLE event_bookings RENAME COLUMN moneda TO currency;
ALTER TABLE event_bookings RENAME COLUMN estado TO status;
ALTER TABLE event_bookings RENAME COLUMN expira_at TO expires_at;
ALTER TABLE event_bookings RENAME COLUMN cancelada_at TO cancelled_at;
ALTER TABLE event_attendees RENAME COLUMN reserva_evento_id TO event_booking_id;
ALTER TABLE event_attendees RENAME COLUMN nombre_asistente TO attendee_name;
ALTER TABLE event_attendees RENAME COLUMN email_asistente TO attendee_email;
ALTER TABLE event_attendees RENAME COLUMN telefono_asistente TO attendee_phone;
ALTER TABLE event_attendees RENAME COLUMN estado_asistencia TO attendance_status;
ALTER TABLE event_attendees RENAME COLUMN registrado_por TO registered_by;
ALTER TABLE event_attendees RENAME COLUMN registrado_at TO registered_at;
ALTER TABLE session_bookings RENAME COLUMN codigo TO code;
ALTER TABLE session_bookings RENAME COLUMN orden_item_id TO order_item_id;
ALTER TABLE session_bookings RENAME COLUMN cliente_id TO customer_id;
ALTER TABLE session_bookings RENAME COLUMN servicio_id TO service_id;
ALTER TABLE session_bookings RENAME COLUMN profesional_id TO specialist_id;
ALTER TABLE session_bookings RENAME COLUMN inicio_at TO starts_at;
ALTER TABLE session_bookings RENAME COLUMN fin_at TO ends_at;
ALTER TABLE session_bookings RENAME COLUMN modalidad TO modality;
ALTER TABLE session_bookings RENAME COLUMN ubicacion_id TO location_id;
ALTER TABLE session_bookings RENAME COLUMN url_virtual TO virtual_url;
ALTER TABLE session_bookings RENAME COLUMN precio TO price;
ALTER TABLE session_bookings RENAME COLUMN moneda TO currency;
ALTER TABLE session_bookings RENAME COLUMN estado TO status;
ALTER TABLE session_bookings RENAME COLUMN expira_at TO expires_at;
ALTER TABLE session_bookings RENAME COLUMN notas_cliente TO customer_notes;
ALTER TABLE session_bookings RENAME COLUMN cancelada_at TO cancelled_at;
ALTER TABLE booking_status_history RENAME COLUMN tipo_reserva TO reservation_type;
ALTER TABLE booking_status_history RENAME COLUMN reserva_id TO booking_id;
ALTER TABLE booking_status_history RENAME COLUMN estado_anterior TO previous_status;
ALTER TABLE booking_status_history RENAME COLUMN estado_nuevo TO new_status;
ALTER TABLE booking_status_history RENAME COLUMN cambiado_por TO changed_by;
ALTER TABLE booking_status_history RENAME COLUMN motivo TO reason;
ALTER TABLE favorites RENAME COLUMN cliente_id TO customer_id;
ALTER TABLE favorites RENAME COLUMN tipo_entidad TO entity_type;
ALTER TABLE favorites RENAME COLUMN entidad_id TO entity_id;
ALTER TABLE home_sections RENAME COLUMN clave TO section_key;
ALTER TABLE home_sections RENAME COLUMN titulo TO title;
ALTER TABLE home_sections RENAME COLUMN subtitulo TO subtitle;
ALTER TABLE home_sections RENAME COLUMN imagen_url TO image_url;
ALTER TABLE home_sections RENAME COLUMN boton_texto TO button_text;
ALTER TABLE home_sections RENAME COLUMN boton_url TO button_url;
ALTER TABLE home_sections RENAME COLUMN orden TO sort_order;
ALTER TABLE audit_logs RENAME COLUMN usuario_id TO user_id;
ALTER TABLE audit_logs RENAME COLUMN accion TO action;
ALTER TABLE audit_logs RENAME COLUMN entidad TO entity_name;
ALTER TABLE audit_logs RENAME COLUMN entidad_id TO entity_id;
ALTER TABLE audit_logs RENAME COLUMN datos_anteriores TO previous_data;
ALTER TABLE audit_logs RENAME COLUMN datos_nuevos TO new_data;

-- PostgreSQL does not rename dependent object names when a table or column is renamed.
-- Normalize every constraint, standalone index, and identity sequence name.
DO $$
DECLARE
    object_record RECORD;
    mapping_record RECORD;
    normalized_name TEXT;
BEGIN
    FOR object_record IN
        SELECT constraint_info.conname AS object_name,
               namespace_info.nspname AS schema_name,
               table_info.relname AS table_name
        FROM pg_constraint constraint_info
        JOIN pg_class table_info ON table_info.oid = constraint_info.conrelid
        JOIN pg_namespace namespace_info ON namespace_info.oid = table_info.relnamespace
        WHERE namespace_info.nspname = current_schema()
    LOOP
        normalized_name := object_record.object_name;
        FOR mapping_record IN SELECT * FROM (VALUES
            ('usuarios', 'users'),
            ('usuario_roles', 'user_roles'),
            ('ciudades', 'cities'),
            ('ubicaciones', 'locations'),
            ('categorias_evento', 'categories'),
            ('especialidades', 'specialties'),
            ('perfil_clientes', 'customer_profiles'),
            ('perfil_profesionales', 'specialist_profiles'),
            ('redes_profesional', 'professional_social_links'),
            ('imagenes_profesional', 'professional_images'),
            ('profesional_especialidades', 'professional_specialties'),
            ('eventos', 'events'),
            ('evento_categorias', 'event_categories'),
            ('evento_profesionales', 'event_professionals'),
            ('imagenes_evento', 'event_images'),
            ('ocurrencias_evento', 'event_occurrences'),
            ('servicios_1a1', 'one_to_one_services'),
            ('disponibilidad_profesional', 'professional_availability'),
            ('bloqueos_agenda', 'schedule_blocks'),
            ('ordenes_compra', 'purchase_orders'),
            ('orden_items', 'purchase_order_items'),
            ('metodos_pago_cliente', 'customer_payment_methods'),
            ('pagos', 'payments'),
            ('reembolsos', 'refunds'),
            ('recibos_digitales', 'digital_receipts'),
            ('reservas_evento', 'event_bookings'),
            ('asistentes_evento', 'event_attendees'),
            ('reservas_sesion', 'session_bookings'),
            ('historial_estados_reserva', 'booking_status_history'),
            ('favoritos', 'favorites'),
            ('secciones_inicio', 'home_sections'),
            ('auditoria', 'audit_logs'),
            ('perfil_prof', 'specialist_profiles'),
            ('evento', 'event'),
            ('profesional', 'specialist'),
            ('especialidad', 'specialty'),
            ('usuario', 'user'),
            ('ciudad', 'city'),
            ('ubicacion', 'location'),
            ('categoria', 'category'),
            ('orden', 'order'),
            ('pago', 'payment'),
            ('reserva', 'booking'),
            ('servicio', 'service'),
            ('cliente', 'customer'),
            ('estado', 'status'),
            ('activo', 'active'),
            ('latitud', 'latitude'),
            ('longitud', 'longitude'),
            ('modalidad', 'modality'),
            ('tipo', 'type'),
            ('formato', 'format'),
            ('peso', 'size'),
            ('precio', 'price'),
            ('importe', 'amount'),
            ('fecha', 'date'),
            ('fechas', 'dates'),
            ('cupos', 'spots'),
            ('horas', 'hours'),
            ('vigencia', 'validity'),
            ('dia', 'day'),
            ('entidad', 'entity'),
            ('rol', 'role'),
            ('inicio', 'start'),
            ('metodo_pago_cliente_id', 'customer_payment_method_id'),
            ('ciudad_preferida_id', 'preferred_city_id'),
            ('url_reserva_externa', 'external_reservation_url'),
            ('referencia_externa', 'external_reference'),
            ('telefono_asistente', 'attendee_phone'),
            ('descripcion_compra', 'purchase_description'),
            ('estado_publicacion', 'publication_status'),
            ('estado_aprobacion', 'approval_status'),
            ('estado_asistencia', 'attendance_status'),
            ('texto_alternativo', 'alternative_text'),
            ('reserva_evento_id', 'event_booking_id'),
            ('telefono_whatsapp', 'whatsapp_phone'),
            ('duracion_minutos', 'duration_minutes'),
            ('cupos_reservados', 'reserved_spots'),
            ('datos_anteriores', 'previous_data'),
            ('ultimo_acceso_at', 'last_login_at'),
            ('token_encriptado', 'encrypted_token'),
            ('fecha_nacimiento', 'birth_date'),
            ('nombre_asistente', 'attendee_name'),
            ('descuento_total', 'total_discount'),
            ('estado_anterior', 'previous_status'),
            ('email_asistente', 'attendee_email'),
            ('precio_unitario', 'unit_price'),
            ('especialidad_id', 'specialty_id'),
            ('registrado_por', 'registered_by'),
            ('nombre_publico', 'public_name'),
            ('cliente_nombre', 'customer_name'),
            ('predeterminado', 'default_method'),
            ('profesional_id', 'specialist_id'),
            ('motivo_rechazo', 'rejection_reason'),
            ('orden_item_id', 'order_item_id'),
            ('importe_total', 'total_amount'),
            ('notas_cliente', 'customer_notes'),
            ('mensaje_error', 'error_message'),
            ('ocurrencia_id', 'occurrence_id'),
            ('rol_en_evento', 'role'),
            ('registrado_at', 'registered_at'),
            ('cliente_email', 'customer_email'),
            ('referencia_id', 'reference_id'),
            ('email_publico', 'public_email'),
            ('vigente_hasta', 'valid_until'),
            ('vigente_desde', 'valid_from'),
            ('categoria_id', 'category_id'),
            ('cambiado_por', 'changed_by'),
            ('cancelada_at', 'cancelled_at'),
            ('tipo_entidad', 'entity_type'),
            ('aprobado_por', 'approved_by'),
            ('tipo_reserva', 'reservation_type'),
            ('indicaciones', 'reference'),
            ('ubicacion_id', 'location_id'),
            ('estado_nuevo', 'new_status'),
            ('datos_nuevos', 'new_data'),
            ('precio_desde', 'starting_price'),
            ('procesado_at', 'processed_at'),
            ('aprobado_at', 'approved_at'),
            ('pais_codigo', 'country_code'),
            ('servicio_id', 'service_id'),
            ('edad_minima', 'minimum_age'),
            ('url_virtual', 'virtual_url'),
            ('hora_inicio', 'start_time'),
            ('boton_texto', 'button_text'),
            ('descripcion', 'description'),
            ('peso_bytes', 'size_bytes'),
            ('es_portada', 'cover'),
            ('dia_semana', 'day_of_week'),
            ('reserva_id', 'booking_id'),
            ('usuario_id', 'user_id'),
            ('creado_por', 'created_by'),
            ('entidad_id', 'entity_id'),
            ('cliente_id', 'customer_id'),
            ('imagen_url', 'image_url'),
            ('anulado_at', 'annulled_at'),
            ('emitido_at', 'issued_at'),
            ('apellidos', 'last_names'),
            ('destacado', 'featured'),
            ('proveedor', 'provider'),
            ('boton_url', 'button_url'),
            ('provincia', 'province'),
            ('capacidad', 'capacity'),
            ('biografia', 'biography'),
            ('ciudad_id', 'city_id'),
            ('evento_id', 'event_id'),
            ('subtitulo', 'subtitle'),
            ('modalidad', 'modality'),
            ('inicio_at', 'starts_at'),
            ('tipo_item', 'item_type'),
            ('expira_at', 'expires_at'),
            ('direccion', 'address'),
            ('pagada_at', 'paid_at'),
            ('sitio_web', 'website'),
            ('longitud', 'longitude'),
            ('hora_fin', 'end_time'),
            ('telefono', 'phone'),
            ('orden_id', 'order_id'),
            ('cantidad', 'quantity'),
            ('foto_url', 'photo_url'),
            ('resumen', 'summary'),
            ('latitud', 'latitude'),
            ('formato', 'format'),
            ('importe', 'amount'),
            ('entidad', 'entity_name'),
            ('nombres', 'first_names'),
            ('pago_id', 'payment_id'),
            ('titulo', 'title'),
            ('activo', 'active'),
            ('estado', 'status'),
            ('fin_at', 'ends_at'),
            ('codigo', 'code'),
            ('numero', 'number'),
            ('nombre', 'name'),
            ('precio', 'price'),
            ('rol_id', 'role_id'),
            ('accion', 'action'),
            ('moneda', 'currency'),
            ('motivo', 'reason'),
            ('clave', 'section_key'),
            ('orden', 'sort_order'),
            ('marca', 'brand'),
            ('tipo', 'type'),
            ('nombre', 'name')
        ) AS mappings(old_name, new_name)
        LOOP
            normalized_name := replace(normalized_name, mapping_record.old_name, mapping_record.new_name);
        END LOOP;
        IF normalized_name <> object_record.object_name THEN
            EXECUTE format('ALTER TABLE %I.%I RENAME CONSTRAINT %I TO %I',
                object_record.schema_name, object_record.table_name,
                object_record.object_name, normalized_name);
        END IF;
    END LOOP;

    FOR object_record IN
        SELECT schemaname AS schema_name, tablename AS table_name, indexname AS object_name
        FROM pg_indexes
        WHERE schemaname = current_schema()
    LOOP
        normalized_name := object_record.object_name;
        FOR mapping_record IN SELECT * FROM (VALUES
            ('usuarios', 'users'),
            ('usuario_roles', 'user_roles'),
            ('ciudades', 'cities'),
            ('ubicaciones', 'locations'),
            ('categorias_evento', 'categories'),
            ('especialidades', 'specialties'),
            ('perfil_clientes', 'customer_profiles'),
            ('perfil_profesionales', 'specialist_profiles'),
            ('redes_profesional', 'professional_social_links'),
            ('imagenes_profesional', 'professional_images'),
            ('profesional_especialidades', 'professional_specialties'),
            ('eventos', 'events'),
            ('evento_categorias', 'event_categories'),
            ('evento_profesionales', 'event_professionals'),
            ('imagenes_evento', 'event_images'),
            ('ocurrencias_evento', 'event_occurrences'),
            ('servicios_1a1', 'one_to_one_services'),
            ('disponibilidad_profesional', 'professional_availability'),
            ('bloqueos_agenda', 'schedule_blocks'),
            ('ordenes_compra', 'purchase_orders'),
            ('orden_items', 'purchase_order_items'),
            ('metodos_pago_cliente', 'customer_payment_methods'),
            ('pagos', 'payments'),
            ('reembolsos', 'refunds'),
            ('recibos_digitales', 'digital_receipts'),
            ('reservas_evento', 'event_bookings'),
            ('asistentes_evento', 'event_attendees'),
            ('reservas_sesion', 'session_bookings'),
            ('historial_estados_reserva', 'booking_status_history'),
            ('favoritos', 'favorites'),
            ('secciones_inicio', 'home_sections'),
            ('auditoria', 'audit_logs'),
            ('perfil_prof', 'specialist_profiles'),
            ('evento', 'event'),
            ('profesional', 'specialist'),
            ('especialidad', 'specialty'),
            ('usuario', 'user'),
            ('ciudad', 'city'),
            ('ubicacion', 'location'),
            ('categoria', 'category'),
            ('orden', 'order'),
            ('pago', 'payment'),
            ('reserva', 'booking'),
            ('servicio', 'service'),
            ('cliente', 'customer'),
            ('estado', 'status'),
            ('activo', 'active'),
            ('latitud', 'latitude'),
            ('longitud', 'longitude'),
            ('modalidad', 'modality'),
            ('tipo', 'type'),
            ('formato', 'format'),
            ('peso', 'size'),
            ('precio', 'price'),
            ('importe', 'amount'),
            ('fecha', 'date'),
            ('fechas', 'dates'),
            ('cupos', 'spots'),
            ('horas', 'hours'),
            ('vigencia', 'validity'),
            ('dia', 'day'),
            ('entidad', 'entity'),
            ('rol', 'role'),
            ('inicio', 'start'),
            ('metodo_pago_cliente_id', 'customer_payment_method_id'),
            ('ciudad_preferida_id', 'preferred_city_id'),
            ('url_reserva_externa', 'external_reservation_url'),
            ('referencia_externa', 'external_reference'),
            ('telefono_asistente', 'attendee_phone'),
            ('descripcion_compra', 'purchase_description'),
            ('estado_publicacion', 'publication_status'),
            ('estado_aprobacion', 'approval_status'),
            ('estado_asistencia', 'attendance_status'),
            ('texto_alternativo', 'alternative_text'),
            ('reserva_evento_id', 'event_booking_id'),
            ('telefono_whatsapp', 'whatsapp_phone'),
            ('duracion_minutos', 'duration_minutes'),
            ('cupos_reservados', 'reserved_spots'),
            ('datos_anteriores', 'previous_data'),
            ('ultimo_acceso_at', 'last_login_at'),
            ('token_encriptado', 'encrypted_token'),
            ('fecha_nacimiento', 'birth_date'),
            ('nombre_asistente', 'attendee_name'),
            ('descuento_total', 'total_discount'),
            ('estado_anterior', 'previous_status'),
            ('email_asistente', 'attendee_email'),
            ('precio_unitario', 'unit_price'),
            ('especialidad_id', 'specialty_id'),
            ('registrado_por', 'registered_by'),
            ('nombre_publico', 'public_name'),
            ('cliente_nombre', 'customer_name'),
            ('predeterminado', 'default_method'),
            ('profesional_id', 'specialist_id'),
            ('motivo_rechazo', 'rejection_reason'),
            ('orden_item_id', 'order_item_id'),
            ('importe_total', 'total_amount'),
            ('notas_cliente', 'customer_notes'),
            ('mensaje_error', 'error_message'),
            ('ocurrencia_id', 'occurrence_id'),
            ('rol_en_evento', 'role'),
            ('registrado_at', 'registered_at'),
            ('cliente_email', 'customer_email'),
            ('referencia_id', 'reference_id'),
            ('email_publico', 'public_email'),
            ('vigente_hasta', 'valid_until'),
            ('vigente_desde', 'valid_from'),
            ('categoria_id', 'category_id'),
            ('cambiado_por', 'changed_by'),
            ('cancelada_at', 'cancelled_at'),
            ('tipo_entidad', 'entity_type'),
            ('aprobado_por', 'approved_by'),
            ('tipo_reserva', 'reservation_type'),
            ('indicaciones', 'reference'),
            ('ubicacion_id', 'location_id'),
            ('estado_nuevo', 'new_status'),
            ('datos_nuevos', 'new_data'),
            ('precio_desde', 'starting_price'),
            ('procesado_at', 'processed_at'),
            ('aprobado_at', 'approved_at'),
            ('pais_codigo', 'country_code'),
            ('servicio_id', 'service_id'),
            ('edad_minima', 'minimum_age'),
            ('url_virtual', 'virtual_url'),
            ('hora_inicio', 'start_time'),
            ('boton_texto', 'button_text'),
            ('descripcion', 'description'),
            ('peso_bytes', 'size_bytes'),
            ('es_portada', 'cover'),
            ('dia_semana', 'day_of_week'),
            ('reserva_id', 'booking_id'),
            ('usuario_id', 'user_id'),
            ('creado_por', 'created_by'),
            ('entidad_id', 'entity_id'),
            ('cliente_id', 'customer_id'),
            ('imagen_url', 'image_url'),
            ('anulado_at', 'annulled_at'),
            ('emitido_at', 'issued_at'),
            ('apellidos', 'last_names'),
            ('destacado', 'featured'),
            ('proveedor', 'provider'),
            ('boton_url', 'button_url'),
            ('provincia', 'province'),
            ('capacidad', 'capacity'),
            ('biografia', 'biography'),
            ('ciudad_id', 'city_id'),
            ('evento_id', 'event_id'),
            ('subtitulo', 'subtitle'),
            ('modalidad', 'modality'),
            ('inicio_at', 'starts_at'),
            ('tipo_item', 'item_type'),
            ('expira_at', 'expires_at'),
            ('direccion', 'address'),
            ('pagada_at', 'paid_at'),
            ('sitio_web', 'website'),
            ('longitud', 'longitude'),
            ('hora_fin', 'end_time'),
            ('telefono', 'phone'),
            ('orden_id', 'order_id'),
            ('cantidad', 'quantity'),
            ('foto_url', 'photo_url'),
            ('resumen', 'summary'),
            ('latitud', 'latitude'),
            ('formato', 'format'),
            ('importe', 'amount'),
            ('entidad', 'entity_name'),
            ('nombres', 'first_names'),
            ('pago_id', 'payment_id'),
            ('titulo', 'title'),
            ('activo', 'active'),
            ('estado', 'status'),
            ('fin_at', 'ends_at'),
            ('codigo', 'code'),
            ('numero', 'number'),
            ('nombre', 'name'),
            ('precio', 'price'),
            ('rol_id', 'role_id'),
            ('accion', 'action'),
            ('moneda', 'currency'),
            ('motivo', 'reason'),
            ('clave', 'section_key'),
            ('orden', 'sort_order'),
            ('marca', 'brand'),
            ('tipo', 'type'),
            ('nombre', 'name')
        ) AS mappings(old_name, new_name)
        LOOP
            normalized_name := replace(normalized_name, mapping_record.old_name, mapping_record.new_name);
        END LOOP;
        IF normalized_name <> object_record.object_name THEN
            EXECUTE format('ALTER INDEX %I.%I RENAME TO %I',
                object_record.schema_name, object_record.object_name, normalized_name);
        END IF;
    END LOOP;

    FOR object_record IN
        SELECT namespace_info.nspname AS schema_name, sequence_info.relname AS object_name
        FROM pg_class sequence_info
        JOIN pg_namespace namespace_info ON namespace_info.oid = sequence_info.relnamespace
        WHERE sequence_info.relkind = 'S' AND namespace_info.nspname = current_schema()
    LOOP
        normalized_name := object_record.object_name;
        FOR mapping_record IN SELECT * FROM (VALUES
            ('usuarios', 'users'),
            ('usuario_roles', 'user_roles'),
            ('ciudades', 'cities'),
            ('ubicaciones', 'locations'),
            ('categorias_evento', 'categories'),
            ('especialidades', 'specialties'),
            ('perfil_clientes', 'customer_profiles'),
            ('perfil_profesionales', 'specialist_profiles'),
            ('redes_profesional', 'professional_social_links'),
            ('imagenes_profesional', 'professional_images'),
            ('profesional_especialidades', 'professional_specialties'),
            ('eventos', 'events'),
            ('evento_categorias', 'event_categories'),
            ('evento_profesionales', 'event_professionals'),
            ('imagenes_evento', 'event_images'),
            ('ocurrencias_evento', 'event_occurrences'),
            ('servicios_1a1', 'one_to_one_services'),
            ('disponibilidad_profesional', 'professional_availability'),
            ('bloqueos_agenda', 'schedule_blocks'),
            ('ordenes_compra', 'purchase_orders'),
            ('orden_items', 'purchase_order_items'),
            ('metodos_pago_cliente', 'customer_payment_methods'),
            ('pagos', 'payments'),
            ('reembolsos', 'refunds'),
            ('recibos_digitales', 'digital_receipts'),
            ('reservas_evento', 'event_bookings'),
            ('asistentes_evento', 'event_attendees'),
            ('reservas_sesion', 'session_bookings'),
            ('historial_estados_reserva', 'booking_status_history'),
            ('favoritos', 'favorites'),
            ('secciones_inicio', 'home_sections'),
            ('auditoria', 'audit_logs'),
            ('perfil_prof', 'specialist_profiles'),
            ('evento', 'event'),
            ('profesional', 'specialist'),
            ('especialidad', 'specialty'),
            ('usuario', 'user'),
            ('ciudad', 'city'),
            ('ubicacion', 'location'),
            ('categoria', 'category'),
            ('orden', 'order'),
            ('pago', 'payment'),
            ('reserva', 'booking'),
            ('servicio', 'service'),
            ('cliente', 'customer'),
            ('estado', 'status'),
            ('activo', 'active'),
            ('latitud', 'latitude'),
            ('longitud', 'longitude'),
            ('modalidad', 'modality'),
            ('tipo', 'type'),
            ('formato', 'format'),
            ('peso', 'size'),
            ('precio', 'price'),
            ('importe', 'amount'),
            ('fecha', 'date'),
            ('fechas', 'dates'),
            ('cupos', 'spots'),
            ('horas', 'hours'),
            ('vigencia', 'validity'),
            ('dia', 'day'),
            ('entidad', 'entity'),
            ('rol', 'role'),
            ('inicio', 'start'),
            ('metodo_pago_cliente_id', 'customer_payment_method_id'),
            ('ciudad_preferida_id', 'preferred_city_id'),
            ('url_reserva_externa', 'external_reservation_url'),
            ('referencia_externa', 'external_reference'),
            ('telefono_asistente', 'attendee_phone'),
            ('descripcion_compra', 'purchase_description'),
            ('estado_publicacion', 'publication_status'),
            ('estado_aprobacion', 'approval_status'),
            ('estado_asistencia', 'attendance_status'),
            ('texto_alternativo', 'alternative_text'),
            ('reserva_evento_id', 'event_booking_id'),
            ('telefono_whatsapp', 'whatsapp_phone'),
            ('duracion_minutos', 'duration_minutes'),
            ('cupos_reservados', 'reserved_spots'),
            ('datos_anteriores', 'previous_data'),
            ('ultimo_acceso_at', 'last_login_at'),
            ('token_encriptado', 'encrypted_token'),
            ('fecha_nacimiento', 'birth_date'),
            ('nombre_asistente', 'attendee_name'),
            ('descuento_total', 'total_discount'),
            ('estado_anterior', 'previous_status'),
            ('email_asistente', 'attendee_email'),
            ('precio_unitario', 'unit_price'),
            ('especialidad_id', 'specialty_id'),
            ('registrado_por', 'registered_by'),
            ('nombre_publico', 'public_name'),
            ('cliente_nombre', 'customer_name'),
            ('predeterminado', 'default_method'),
            ('profesional_id', 'specialist_id'),
            ('motivo_rechazo', 'rejection_reason'),
            ('orden_item_id', 'order_item_id'),
            ('importe_total', 'total_amount'),
            ('notas_cliente', 'customer_notes'),
            ('mensaje_error', 'error_message'),
            ('ocurrencia_id', 'occurrence_id'),
            ('rol_en_evento', 'role'),
            ('registrado_at', 'registered_at'),
            ('cliente_email', 'customer_email'),
            ('referencia_id', 'reference_id'),
            ('email_publico', 'public_email'),
            ('vigente_hasta', 'valid_until'),
            ('vigente_desde', 'valid_from'),
            ('categoria_id', 'category_id'),
            ('cambiado_por', 'changed_by'),
            ('cancelada_at', 'cancelled_at'),
            ('tipo_entidad', 'entity_type'),
            ('aprobado_por', 'approved_by'),
            ('tipo_reserva', 'reservation_type'),
            ('indicaciones', 'reference'),
            ('ubicacion_id', 'location_id'),
            ('estado_nuevo', 'new_status'),
            ('datos_nuevos', 'new_data'),
            ('precio_desde', 'starting_price'),
            ('procesado_at', 'processed_at'),
            ('aprobado_at', 'approved_at'),
            ('pais_codigo', 'country_code'),
            ('servicio_id', 'service_id'),
            ('edad_minima', 'minimum_age'),
            ('url_virtual', 'virtual_url'),
            ('hora_inicio', 'start_time'),
            ('boton_texto', 'button_text'),
            ('descripcion', 'description'),
            ('peso_bytes', 'size_bytes'),
            ('es_portada', 'cover'),
            ('dia_semana', 'day_of_week'),
            ('reserva_id', 'booking_id'),
            ('usuario_id', 'user_id'),
            ('creado_por', 'created_by'),
            ('entidad_id', 'entity_id'),
            ('cliente_id', 'customer_id'),
            ('imagen_url', 'image_url'),
            ('anulado_at', 'annulled_at'),
            ('emitido_at', 'issued_at'),
            ('apellidos', 'last_names'),
            ('destacado', 'featured'),
            ('proveedor', 'provider'),
            ('boton_url', 'button_url'),
            ('provincia', 'province'),
            ('capacidad', 'capacity'),
            ('biografia', 'biography'),
            ('ciudad_id', 'city_id'),
            ('evento_id', 'event_id'),
            ('subtitulo', 'subtitle'),
            ('modalidad', 'modality'),
            ('inicio_at', 'starts_at'),
            ('tipo_item', 'item_type'),
            ('expira_at', 'expires_at'),
            ('direccion', 'address'),
            ('pagada_at', 'paid_at'),
            ('sitio_web', 'website'),
            ('longitud', 'longitude'),
            ('hora_fin', 'end_time'),
            ('telefono', 'phone'),
            ('orden_id', 'order_id'),
            ('cantidad', 'quantity'),
            ('foto_url', 'photo_url'),
            ('resumen', 'summary'),
            ('latitud', 'latitude'),
            ('formato', 'format'),
            ('importe', 'amount'),
            ('entidad', 'entity_name'),
            ('nombres', 'first_names'),
            ('pago_id', 'payment_id'),
            ('titulo', 'title'),
            ('activo', 'active'),
            ('estado', 'status'),
            ('fin_at', 'ends_at'),
            ('codigo', 'code'),
            ('numero', 'number'),
            ('nombre', 'name'),
            ('precio', 'price'),
            ('rol_id', 'role_id'),
            ('accion', 'action'),
            ('moneda', 'currency'),
            ('motivo', 'reason'),
            ('clave', 'section_key'),
            ('orden', 'sort_order'),
            ('marca', 'brand'),
            ('tipo', 'type'),
            ('nombre', 'name')
        ) AS mappings(old_name, new_name)
        LOOP
            normalized_name := replace(normalized_name, mapping_record.old_name, mapping_record.new_name);
        END LOOP;
        IF normalized_name <> object_record.object_name THEN
            EXECUTE format('ALTER SEQUENCE %I.%I RENAME TO %I',
                object_record.schema_name, object_record.object_name, normalized_name);
        END IF;
    END LOOP;
END
$$;
