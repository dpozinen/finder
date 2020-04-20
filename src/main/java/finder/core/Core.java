package finder.core;

/*
 TODO: fix issue with autowiring the repo and setting the out collection of the BFSCore so that the results get updated
  and the core being responsible for saving the pages to repo etc.
 */
public interface Core {
	void find();
	void pause();
	void stop();
	void play();
}
