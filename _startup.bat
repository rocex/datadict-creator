@echo off

set ddc_home=%~dp0
set java_exec=java

::;%ddc_home%\out
set class_path=%ddc_home%\lib\*

%java_exec% -Dfile.encoding=UTF-8 -cp "%class_path%" org.rocex.datadict.DataDictCreator
