CMP= javac
LIBS= -cp /usr/share/java/bukkitapi-latest.jar
FLAGS=-Xlint:unchecked

all: targets
	$(CMP) $(LIBS) $(FLAGS) @sourcefiles
	@rm sourcefiles

targets:
	@echo "" > sourcefiles
	@ls | grep -e .java$ >> sourcefiles

clean:
	rm -f *.class 
