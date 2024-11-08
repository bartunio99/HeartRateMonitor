idf_component_register(SRCS "HeartRateMonitor.c"
                    INCLUDE_DIRS ".")
                    component_compile_options(-Wno-error=format= -Wno-format)
