-- Data for Name: cities
INSERT INTO public.cities (istat_id, cap, city, geojson, geoname_id, province, region) VALUES (72006, '701xx', 'Bari', NULL, NULL, 'BA', NULL);

-- Data for Name: organizations
INSERT INTO public.organizations (id, code, geojson, initiative_logo, logo, notification_logo, page_description, regulation, terms_conditions, title, type, validation, validator_email) VALUES (1, 'AA', NULL, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', NULL, NULL, NULL, NULL, NULL, 'Organizzazione AA (PA)', 0, false, NULL);
INSERT INTO public.organizations (id, code, geojson, initiative_logo, logo, notification_logo, page_description, regulation, terms_conditions, title, type, validation, validator_email) VALUES (2, 'BB', NULL, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', NULL, NULL, NULL, NULL, NULL, 'Organizzazione BB (COMPANY)', 1, false, NULL);

SELECT pg_catalog.setval('public.organizations_seq_id', 3, false);

-- Data for Name: organization_settings
INSERT INTO public.organization_settings (name, default_value) VALUES ('multiplier', '1');
INSERT INTO public.organization_settings (name, default_value) VALUES ('isActiveUrbanPoints', 'false');
INSERT INTO public.organization_settings (name, default_value) VALUES ('startDateUrbanPoints', '2000-01-01');
INSERT INTO public.organization_settings (name, default_value) VALUES ('endDateUrbanPoints', '2100-01-01');
INSERT INTO public.organization_settings (name, default_value) VALUES ('isActiveUrbanPathRefunds', 'false');
INSERT INTO public.organization_settings (name, default_value) VALUES ('euroValueKmUrbanPathBike', '0.04');
INSERT INTO public.organization_settings (name, default_value) VALUES ('isActiveTimeSlotBonus', 'false');
INSERT INTO public.organization_settings (name, default_value) VALUES ('startDateBonus', '2021-12-01');
INSERT INTO public.organization_settings (name, default_value) VALUES ('endDateBonus', '2021-12-31');
INSERT INTO public.organization_settings (name, default_value) VALUES ('isActiveHomeWorkRefunds', 'false');
INSERT INTO public.organization_settings (name, default_value) VALUES ('valueKmHomeWorkBike', '0.20');
INSERT INTO public.organization_settings (name, default_value) VALUES ('homeWorkRefundType', null);
INSERT INTO public.organization_settings (name, default_value) VALUES ('euroMaxRefundInATime', null);
INSERT INTO public.organization_settings (name, default_value) VALUES ('euroMaxRefundInAMonth', null);
INSERT INTO public.organization_settings (name, default_value) VALUES ('euroMaxRefundInADay', null);
INSERT INTO public.organization_settings (name, default_value) VALUES ('homeWorkPathTolerancePerc', '1');
INSERT INTO public.organization_settings (name, default_value) VALUES ('homeWorkPointsTolerance', '0.05');

-- Data for Name: organization_setting_values

-- Data for Name: seats
INSERT INTO public.seats (id, created_date, last_modified_date, address, latitude, longitude, number, validated, city_istat_id, organization_id, name, deleted, destination_tolerance) VALUES (1, '2021-07-19 13:25:24.494001 +00:00', '2021-08-24 08:26:22.149719 +00:00', 'Via Napoli', 41.128527492790454, 16.83633675502973, '328', true, 72006, 1, '', false, 0.10);

SELECT pg_catalog.setval('public.seat_seq_id', 2, false);

-- Data for Name: users
INSERT INTO public.users (uid, created_date, last_modified_date, active_carpooling, avatar_url, birth_date, car_model, city_id, city_pin_bike, current_rank, date_last_cup_won, earned_national_points, email, email_verified, enabled, first_name, fiscal_code, gender, iban, last_logged_in, last_name, number_cups_won, number_national_awards_won, number_rankings_won, number_reports_made, number_tracks_saved, phone_number, position_national_ranking, scooter_firmware, scooter_model, signup_completed, total_money_earned, total_money_refund_home_work, total_money_refund_not_home_work, total_rank, user_type, username, home_address_id, organization_id, marketing_terms_accepted, terms_accepted, car_id, old_user_id, reset_password_required, euro, points, coin, coin_wallet) VALUES ('U53R0007WDXuDp5WAH0f97mUwxH2', null, '2021-11-27 22:17:50.442783 +00:00', null, null, '2000-09-07', null, null, null, null, null, 0, 'user0@nextome.net', true, true, 'John', null, 'Maschio', null, '2021-11-14 10:47:30.036475 +00:00', 'Doe ', null, null, null, null, null, null, null, null, null, true, null, null, null, null, 3, 'JohnDoe', null, null, false, true, null, null, false, null, null, null, null);
INSERT INTO public.users (uid, created_date, last_modified_date, active_carpooling, avatar_url, birth_date, car_model, city_id, city_pin_bike, current_rank, date_last_cup_won, earned_national_points, email, email_verified, enabled, first_name, fiscal_code, gender, iban, last_logged_in, last_name, number_cups_won, number_national_awards_won, number_rankings_won, number_reports_made, number_tracks_saved, phone_number, position_national_ranking, scooter_firmware, scooter_model, signup_completed, total_money_earned, total_money_refund_home_work, total_money_refund_not_home_work, total_rank, user_type, username, home_address_id, organization_id, marketing_terms_accepted, terms_accepted, car_id, old_user_id, reset_password_required, euro, points, coin, coin_wallet) VALUES ('U53R0017WDXuDp5WAH0f97mUwxH2', null, '2021-11-27 22:17:50.442783 +00:00', null, null, '2000-09-07', null, null, null, null, null, 0, 'user1@nextome.net', true, true, 'John', null, 'Maschio', null, '2021-11-14 10:47:30.036475 +00:00', 'Doe ', null, null, null, null, null, null, null, null, null, true, null, null, null, null, 3, 'JohnDoe', null, null, false, true, null, null, false, null, null, null, null);

-- Data for Name: enrollments
INSERT INTO public.enrollments (id, code, organization_id, user_uid, created_date, last_modified_date, activation_date, end_date, start_date, points, euro, session_forwarding) VALUES (1, 'AA65CA1E', 1, 'U53R0017WDXuDp5WAH0f97mUwxH2', '2021-12-10 11:07:30.771592 +00:00', '2021-12-10 11:07:30.771592 +00:00', null, '2100-01-01', '2021-12-01', null, null, false);
INSERT INTO public.enrollments (id, code, organization_id, user_uid, created_date, last_modified_date, activation_date, end_date, start_date, points, euro, session_forwarding) VALUES (2, 'BB65CA1E', 2, 'U53R0017WDXuDp5WAH0f97mUwxH2', '2021-12-10 11:07:30.771592 +00:00', '2021-12-10 11:07:30.771592 +00:00', null, '2100-01-01', '2021-12-01', null, null, false);

SELECT pg_catalog.setval('public.enroll_id_seq', 3, false);

-- Data for Name: home_addresses
INSERT INTO public.home_addresses (id, created_date, last_modified_date, address, latitude, longitude, number, end_association, start_association, city_istat_id, user_uid) VALUES (1, '2021-09-16 07:23:56.245968 +00:00', '2021-09-16 07:23:56.245968 +00:00', 'Via Giovanni Amendola', 41.11687074767688, 16.87841216274474, '27', null, '2021-09-16 07:23:56.245588 +00:00', 72006, 'U53R0017WDXuDp5WAH0f97mUwxH2');
UPDATE public.users SET home_address_id=1 WHERE uid='U53R0007WDXuDp5WAH0f97mUwxH2';
UPDATE public.users SET home_address_id=1 WHERE uid='U53R0017WDXuDp5WAH0f97mUwxH2';

SELECT pg_catalog.setval('public.homeaddresses_seq_id', 2, false);

-- Data for Name: work_addresses
INSERT INTO public.work_addresses (id, end_association, start_association, seat_id, user_uid) VALUES (1, NULL, '2021-12-10 07:26:19.763131+00', 1, 'U53R0017WDXuDp5WAH0f97mUwxH2');

SELECT pg_catalog.setval('public.workaddresses_seq_id', 2, false);

-- Data for Name: home_work_paths
INSERT INTO public.home_work_paths (id, distance, polyline, home_address_id, seat_id, user_uid) VALUES (1, 4.43800, 'ss}yFqq_fBaCdBRpHJrDXnKF|Bo@L]DkBHa@FsDJkBFOIg@FqH^cG^aCJ}CPkBNaCHFzDV|IZbNj@bU@TKTUZWLmANgBHmBBMpLYrME~DMlIYjOGhHMzKQtKYdSc@pYC`AI`AWrA', 1, 1, 'U53R0017WDXuDp5WAH0f97mUwxH2');

SELECT pg_catalog.setval('public.paths_seq_id', 2, false);

-- Data for Name: sensors
INSERT INTO public.sensors (uuid, bike_type, end_association, firmware, start_association, stolen, wheel_diameter, user_uid, id, ugo_id, name) VALUES ('S3:N5:0R:00:00:00', 0, null, 'V3.11.0', '2021-06-01 17:22:25.050610 +00:00', false, 700, 'U53R0017WDXuDp5WAH0f97mUwxH2', 50, null, 'Lis Move k2');

SELECT pg_catalog.setval('public.sensor_seq_id', 2, false);

-- Data for Name: smartphones
INSERT INTO public.smartphones (id, app_version, end_association, start_association, imei, user_uid, model, platform, fcm_token) VALUES (1, '2.3.0-beta', null, '2021-10-31 09:14:29.731001 +00:00', 'fBMOQgDeQKOSzsn4fOg-zj', 'U53R0017WDXuDp5WAH0f97mUwxH2', 'iPhone 20', 'iOS', null);

SELECT pg_catalog.setval('public.smartphone_id_seq', 2, false);

-- Data for Name: sessions
-- 1) nationalPoints
INSERT INTO public.sessions (id, description, end_battery, end_time, euro, gps_distance, gyro_distance, is_home_work_path, multiplier, national_km, national_points, start_battery, start_time, total_km, valid, user_uid, certificated, created_date, last_modified_date, status, polyline, bike_type, wheel_diameter, home_address_id, work_address_id, gmaps_distance, phone_end_battery, phone_start_battery, co2, firmware, sensor, duration, type, urban_km, sensor_name, urban_points, app_version, platform, gps_only_distance, old_session_id, validated_date, phone_model, verification_required, verification_required_note, forwarded_at, gmaps_polyline, raw_polyline) VALUES ('123e4567-7c32-4e4d-a105-1536a6120000', 'Sessione di test del 1635674343710', 42, '2021-12-01 09:00:00.000000 +00:00', null, 2.32059, 2.33282, false, null, 2.32282, 23, 49, '2021-10-31 09:59:03.710000 +00:00', null, true, 'U53R0007WDXuDp5WAH0f97mUwxH2', true, '2021-10-31 10:25:48.584872 +00:00', '2021-10-31 10:25:50.625632 +00:00', 0, null, 'Tradizionale (muscolare)', 660.00000, 1, 1, 2.45600, 46, 51, 756.4161700000001, 'V3.11.0', 'S3:N5:0R:00:00:00', 540, 0, null, 'Lis Move k2', null, '2.3.0-beta', 'iOS', 0.00000, null, '2021-10-31 10:25:50.624812', 'iPhone 20', null, null, null, null, null);
-- 2) urbanPoints
INSERT INTO public.sessions (id, description, end_battery, end_time, euro, gps_distance, gyro_distance, is_home_work_path, multiplier, national_km, national_points, start_battery, start_time, total_km, valid, user_uid, certificated, created_date, last_modified_date, status, polyline, bike_type, wheel_diameter, home_address_id, work_address_id, gmaps_distance, phone_end_battery, phone_start_battery, co2, firmware, sensor, duration, type, urban_km, sensor_name, urban_points, app_version, platform, gps_only_distance, old_session_id, validated_date, phone_model, verification_required, verification_required_note, forwarded_at, gmaps_polyline, raw_polyline) VALUES ('123e4567-7c32-4e4d-a105-1536a6120001', 'Sessione di test del 1635674343710', 42, '2021-12-01 10:00:00.000000 +00:00', null, 2.32059, 2.33282, false, null, 2.32282, 23, 49, '2021-10-31 09:59:03.710000 +00:00', null, true, 'U53R0017WDXuDp5WAH0f97mUwxH2', true, '2021-10-31 10:25:48.584872 +00:00', '2021-10-31 10:25:50.625632 +00:00', 0, null, 'Tradizionale (muscolare)', 660.00000, 1, 1, 2.45600, 46, 51, 756.4161700000001, 'V3.11.0', 'S3:N5:0R:00:00:00', 540, 0, null, 'Lis Move k2', null, '2.3.0-beta', 'iOS', 0.00000, null, '2021-10-31 10:25:50.624812', 'iPhone 20', null, null, null, null, null);
-- 3) urbanPointsWithEuroRefundTest
INSERT INTO public.sessions (id, description, end_battery, end_time, euro, gps_distance, gyro_distance, is_home_work_path, multiplier, national_km, national_points, start_battery, start_time, total_km, valid, user_uid, certificated, created_date, last_modified_date, status, polyline, bike_type, wheel_diameter, home_address_id, work_address_id, gmaps_distance, phone_end_battery, phone_start_battery, co2, firmware, sensor, duration, type, urban_km, sensor_name, urban_points, app_version, platform, gps_only_distance, old_session_id, validated_date, phone_model, verification_required, verification_required_note, forwarded_at, gmaps_polyline, raw_polyline) VALUES ('123e4567-7c32-4e4d-a105-1536a6120002', 'Sessione di test del 1635674343710', 42, '2021-12-01 11:00:00.000000 +00:00', null, 2.32059, 2.33282, false, null, 2.32282, 23, 49, '2021-10-31 09:59:03.710000 +00:00', null, true, 'U53R0017WDXuDp5WAH0f97mUwxH2', true, '2021-10-31 10:25:48.584872 +00:00', '2021-10-31 10:25:50.625632 +00:00', 0, null, 'Tradizionale (muscolare)', 660.00000, 1, 1, 2.45600, 46, 51, 756.4161700000001, 'V3.11.0', 'S3:N5:0R:00:00:00', 540, 0, null, 'Lis Move k2', null, '2.3.0-beta', 'iOS', 0.00000, null, '2021-10-31 10:25:50.624812', 'iPhone 20', null, null, null, null, null);
-- 4) homeWorkPointsWithUrbanPointsRefundTest
INSERT INTO public.sessions (id, description, end_battery, end_time, euro, gps_distance, gyro_distance, is_home_work_path, multiplier, national_km, national_points, start_battery, start_time, total_km, valid, user_uid, certificated, created_date, last_modified_date, status, polyline, bike_type, wheel_diameter, home_address_id, work_address_id, gmaps_distance, phone_end_battery, phone_start_battery, co2, firmware, sensor, duration, type, urban_km, sensor_name, urban_points, app_version, platform, gps_only_distance, old_session_id, validated_date, phone_model, verification_required, verification_required_note, forwarded_at, gmaps_polyline, raw_polyline) VALUES ('123e4567-7c32-4e4d-a105-1536a6120003', 'Sessione di test del 1635674343710', 42, '2021-12-02 12:00:00.000000 +00:00', null, 4.64059, 4.65282, true, null, 4.65282, 46, 49, '2021-10-31 09:59:03.710000 +00:00', null, true, 'U53R0017WDXuDp5WAH0f97mUwxH2', true, '2021-10-31 10:25:48.584872 +00:00', '2021-10-31 10:25:50.625632 +00:00', 0, null, 'Tradizionale (muscolare)', 660.00000, 1, 1, 4.90600, 46, 51, 756.4161700000001, 'V3.11.0', 'S3:N5:0R:00:00:00', 1087, 0, null, 'Lis Move k2', null, '2.3.0-beta', 'iOS', 0.00000, null, '2021-10-31 10:25:50.624812', 'iPhone 20', null, null, null, null, null);
-- 5) homeWorkPointsWithEuroRefundTest
INSERT INTO public.sessions (id, description, end_battery, end_time, euro, gps_distance, gyro_distance, is_home_work_path, multiplier, national_km, national_points, start_battery, start_time, total_km, valid, user_uid, certificated, created_date, last_modified_date, status, polyline, bike_type, wheel_diameter, home_address_id, work_address_id, gmaps_distance, phone_end_battery, phone_start_battery, co2, firmware, sensor, duration, type, urban_km, sensor_name, urban_points, app_version, platform, gps_only_distance, old_session_id, validated_date, phone_model, verification_required, verification_required_note, forwarded_at, gmaps_polyline, raw_polyline) VALUES ('123e4567-7c32-4e4d-a105-1536a6120004', 'Sessione di test del 1635674343710', 42, '2021-12-03 13:00:00.000000 +00:00', null, 4.64059, 4.65282, true, null, 4.65282, 46, 49, '2021-10-31 09:59:03.710000 +00:00', null, true, 'U53R0017WDXuDp5WAH0f97mUwxH2', true, '2021-10-31 10:25:48.584872 +00:00', '2021-10-31 10:25:50.625632 +00:00', 0, null, 'Tradizionale (muscolare)', 660.00000, 1, 1, 4.90600, 46, 51, 756.4161700000001, 'V3.11.0', 'S3:N5:0R:00:00:00', 1087, 0, null, 'Lis Move k2', null, '2.3.0-beta', 'iOS', 0.00000, null, '2021-10-31 10:25:50.624812', 'iPhone 20', null, null, null, null, null);

INSERT INTO public.sessions (id, description, end_battery, end_time, euro, gps_distance, gyro_distance, is_home_work_path, multiplier, national_km, national_points, start_battery, start_time, total_km, valid, user_uid, certificated, created_date, last_modified_date, status, polyline, bike_type, wheel_diameter, home_address_id, work_address_id, gmaps_distance, phone_end_battery, phone_start_battery, co2, firmware, sensor, duration, type, urban_km, sensor_name, urban_points, app_version, platform, gps_only_distance, old_session_id, validated_date, phone_model, verification_required, verification_required_note, forwarded_at, gmaps_polyline, raw_polyline) VALUES ('123e4567-7c32-4e4d-a105-1536a6120005', 'Sessione di test del 1635674343710', 42, '2021-12-03 14:00:00.000000 +00:00', null, 4.64059, 4.65282, true, null, 4.65282, 46, 49, '2021-10-31 09:59:03.710000 +00:00', null, true, 'U53R0017WDXuDp5WAH0f97mUwxH2', true, '2021-10-31 10:25:48.584872 +00:00', '2021-10-31 10:25:50.625632 +00:00', 0, null, 'Tradizionale (muscolare)', 660.00000, 1, 1, 4.90600, 46, 51, 756.4161700000001, 'V3.11.0', 'S3:N5:0R:00:00:00', 1087, 0, null, 'Lis Move k2', null, '2.3.0-beta', 'iOS', 0.00000, null, '2021-10-31 10:25:50.624812', 'iPhone 20', null, null, null, null, null);

-- Data for Name: partials (non necessari)

-- Data for Name: session_points
INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (1, 23, 1, '123e4567-7c32-4e4d-a105-1536a6120001', 2.32282, 1, null, null, null, null, null, null);
INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (2, 10, 2, '123e4567-7c32-4e4d-a105-1536a6120001', 1.02282, 1, null, null, null, null, null, null);

INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (3, 23, 1, '123e4567-7c32-4e4d-a105-1536a6120002', 2.32282, 1, null, null, null, null, null, null);
INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (4, 5, 2, '123e4567-7c32-4e4d-a105-1536a6120002', 0.52282, 1, null, null, null, null, null, null);

INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (5, 46, 1, '123e4567-7c32-4e4d-a105-1536a6120003', 4.65282, 1, null, null, null, null, null, null);
INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (6, 46, 2, '123e4567-7c32-4e4d-a105-1536a6120003', 4.65282, 1, null, null, null, null, null, null);

INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (7, 46, 1, '123e4567-7c32-4e4d-a105-1536a6120004', 4.65282, 1, null, null, null, null, null, null);
INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (8, 46, 2, '123e4567-7c32-4e4d-a105-1536a6120004', 4.65282, 1, null, null, null, null, null, null);

INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (9, 46, 1, '123e4567-7c32-4e4d-a105-1536a6120005', 4.65282, 1, null, null, null, null, null, null);
INSERT INTO public.session_points (id, points, organization_id, session_id, distance, multiplier, euro, refund_status, multiplier_distance, multiplier_points, refund_distance, home_work_distance) VALUES (10, 46, 2, '123e4567-7c32-4e4d-a105-1536a6120005', 4.65282, 1, null, null, null, null, null, null);

SELECT pg_catalog.setval('public.session_points_id_seq', 11, false);