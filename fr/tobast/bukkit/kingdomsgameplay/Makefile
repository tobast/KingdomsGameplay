CMP= javac
LIBS= -cp /usr/share/java/bukkitapi-latest.jar
FLAGS=-Xlint:unchecked -source 6 -target 6 -Xlint:deprecation

all: targets
	$(CMP) $(LIBS) $(FLAGS) @sourcefiles
	@rm sourcefiles

targets:
	@echo "" > sourcefiles
	@ls | grep -e .java$ >> sourcefiles

clean:
	rm -f *.class 
