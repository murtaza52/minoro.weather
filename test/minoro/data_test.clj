(ns minoro.data-test
  "Sample data for testing")

(def current-weather-data {:coord     {:lon 23.72, :lat 37.98},
                           :name       "Athens",
                           :dt         1578830944,
                           :wind       {:speed 3.6, :deg 330},
                           :id         264371,
                           :weather
                           [{:id 802, :main "Clouds", :description "scattered clouds", :icon "03d"}],
                           :clouds     {:all 40},
                           :sys
                           {:country "GR", :timezone 7200, :sunrise 1578807644, :sunset 1578842708},
                           :main
                           {:temp       13.01,
                            :feels_like 9.16,
                            :temp_min   11.11,
                            :temp_max   14.44,
                            :pressure   1023,
                            :humidity   1},
                           :visibility 10000})

(def processed-weather-data ["Athens_GR_264371" "2020-01-12 00:00:00.0" "2020-01-12 14:09:04.0" "Athens" 264371 "GR" 7200 "2020-01-12 07:40:44.0" "2020-01-12 17:25:08.0" 3.6 330 40 13.01 9.16 11.11 14.44 1023 1 10000])

(def forecast-data {:sunset 1578929169,
                    :coord {:lat 37.9795, :lon 23.7162},
                    :dt_txt "2020-01-13 18:00:00",
                    :timezone 7200,
                    :name "Athens",
                    :dt 1578938400,
                    :sunrise 1578894032,
                    :wind {:speed 2.68, :deg 11},
                    :id 264371,
                    :weather [{:id 800, :main "Clear", :description "clear sky", :icon "01n"}],
                    :clouds {:all 7},
                    :sys {:pod "n"},
                    :main
                    {:sea_level 1026,
                     :pressure 1026,
                     :temp 9.59,
                     :temp_max 9.71,
                     :temp_min 9.59,
                     :temp_kf -0.12,
                     :humidity 67,
                     :feels_like 6.35,
                     :grnd_level 1013},
                    :country "GR"})

(def processed-forecast-data ["Athens_GR_264371" "2020-01-13 20:00:00.0" "2020-01-13 00:00:00.0" "2020-01-13 20:00:00.0" "Athens" 264371 "GR" 7200 2.68 11 7 9.59 6.35 9.59 9.71 1026 67 1026 1013])
