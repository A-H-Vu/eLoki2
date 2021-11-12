package scraper;


/**
 * Class for the urls in the queue, with a depth parameter to track depth of urls crawled
 * and stop once the max-depth is reached.
 * @author Allen
 *
 */
public class QueueURL {

	public String url;
	//Tracks the depth of the url, or the minimum number of links you need to click to get to the page
	//from the base url page. 0 for the baseUrl
	public int depth;
	//public String prevUrl;
	public QueueURL(String url, int depth){
		this.url = url;
		this.depth = depth;
	}
	public String toString(){
		return url;
	}
}
