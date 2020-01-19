create table current_weather(
  gen_city_id varchar,
  date timestamp,
  date_time timestamp,
  city_name varchar,
  city_id integer,
  country_code varchar,
  timezone varchar,
  sunrise timestamp,
  sunset timestamp,
  wind_speed varchar,
  wind_deg varchar,
  cloudiness integer,
  temp numeric,
  feels_like numeric,
  temp_min numeric,
  temp_max numeric,
  pressure numeric,
  humidity numeric,
  visibility integer
);

create table average_daily_temperature(
  gen_city_id varchar,
  date timestamp,
  average_temp numeric
  );

create table latest_weather(
  gen_city_id varchar,
  date timestamp,
  date_time timestamp,
  city_name varchar,
  city_id integer,
  country_code varchar,
  timezone varchar,
  sunrise timestamp,
  sunset timestamp,
  wind_speed varchar,
  wind_deg varchar,
  cloudiness integer,
  temp numeric,
  feels_like numeric,
  temp_min numeric,
  temp_max numeric,
  pressure numeric,
  humidity numeric,
  visibility integer
);

create table forecast(
  gen_city_id varchar,
  date timestamp,
  forecast_date timestamp,
  forecast_date_time timestamp,
  city_name varchar,
  city_id integer,
  country_code varchar,
  timezone varchar,
  wind_speed varchar,
  wind_deg varchar,
  cloudiness integer,
  temp numeric,
  feels_like numeric,
  temp_min numeric,
  temp_max numeric,
  pressure numeric,
  humidity numeric,
  pressure_sea_level integer,
  pressure_grnd_level integer
);

create table daily_temperature(
  date timestamp,
  gen_city_id varchar,
  min_temp numeric,
  max_temp numeric,
  forecasted_temps numeric []
  );


\copy current_weather(gen_city_id, date, date_time, city_name, city_id, country_code, timezone, sunrise, sunset, wind_speed, wind_deg, cloudiness, temp, feels_like, temp_min, temp_max, pressure, humidity, visibility) from '/home/murtaza/repos/minoro.weather/reports/current_weather_2020-01-14T12:49:25.237.csv' DELIMITER ',' CSV HEADER;

\copy forecast(gen_city_id, date, forecast_date, forecast_date_time, city_name, city_id, country_code, timezone, wind_speed, wind_deg, cloudiness, temp, feels_like, temp_min, temp_max, pressure, humidity, pressure_sea_level, pressure_grnd_level) from '/home/murtaza/repos/minoro.weather/reports/forecast_2020-01-15T11:23:03.846.csv' DELIMITER ',' CSV HEADER;

INSERT INTO average_daily_temperature (gen_city_id, date, average_temp) SELECT gen_city_id, date, AVG(temp) AS average_temp FROM current_weather cw WHERE date < current_date GROUP BY gen_city_id, date ORDER BY date;

insert into latest_weather (gen_city_id, date, date_time, city_name, city_id, country_code, timezone, sunrise, sunset, wind_speed, wind_deg, cloudiness, temp, feels_like, temp_min, temp_max, pressure, humidity, visibility) select distinct on (gen_city_id) gen_city_id, date, date_time, city_name, city_id, country_code, timezone, sunrise, sunset, wind_speed, wind_deg, cloudiness, temp, feels_like, temp_min, temp_max, pressure, humidity, visibility from current_weather order by gen_city_id, date_time desc;

delete from latest_weather;

insert into daily_temperature(date, gen_city_id, min_temp, max_temp, forecasted_temps)
select cw.date, f.gen_city_id, min_temp, max_temp, forecasted_temps from
             (select date, gen_city_id, min(temp) as min_temp, max(temp) as max_temp from current_weather cw group by date, gen_city_id order by date) cw
             inner join
             (select gen_city_id, forecast_date, array_agg(temp) as forecasted_temps from forecast where forecast_date < current_date group by gen_city_id, forecast_date) f
             on cw.gen_city_id = f.gen_city_id and cw.date = f.forecast_date;

