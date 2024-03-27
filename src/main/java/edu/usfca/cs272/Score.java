package edu.usfca.cs272;

import java.nio.file.Path;

/** ScoreMap class to store the result */
public class Score implements Comparable<Score> {
  private Integer count;
  private Double score;
  private String where;

  /**
   * Constructor for the ScoreMap
   *
   * @param count number of times a query word was present in the file.
   * @param score score of the query in the file.
   * @param where the file where the query was searched.
   */
  public Score(int count, double score, String where) {
    this.count = count;
    this.score = score;
    this.where = where;
  }

  /** Constructor for ScoreMap. Defaults Numbers to 0 and where to null.` */
  public Score() {
    this.count = 0;
    this.score = 0.0;
    this.where = "";
  }

  /**
   * Constructor for Score with a given location. This constructor is needed because our
   * requirements call for unique location values in the scoreList of a query.
   *
   * @param location file path
   */
  public Score(String location) {
    this.count = 0;
    this.score = 0.0;
    this.where = location;
  }

  /**
   * getter for count
   *
   * @return count
   */
  public Integer getCount() {
    return count;
  }

  /**
   * setter for count
   *
   * @param count number of times query stem is present in the file.
   */
  public void setCount(int count) {
    this.count = count;
  }

  /**
   * getter for score.
   *
   * @return the score of the query in the file.
   */
  public Double getScore() {
    return score;
  }

  /**
   * sets the score for the query
   *
   * @param score score of the query. given by counts/total stems in file.
   */
  public void setScore(double score) {
    this.score = score;
  }

  /**
   * getter for where
   *
   * @return the file path
   */
  public String getWhere() {
    return where;
  }

  /**
   * setter for where
   *
   * @param where file path
   */
  public void setWhere(String where) {
    this.where = where;
  }

  @Override
  public int compareTo(Score other) {
    int scoreCompare = other.getScore().compareTo(this.getScore());
    if (scoreCompare == 0) {
      int countCompare = other.getCount().compareTo(this.getCount());
      if (countCompare == 0) {
        return Path.of(this.getWhere()).compareTo(Path.of(other.getWhere()));
      } else return countCompare;
    } else return scoreCompare;
  }

  @Override
  public String toString() {
    return "ScoreMap{" + "count=" + count + ", score=" + score + ", where='" + where + '\'' + '}';
  }
}

/*
 * TODO Warnings!
 * 
Description	Resource	Path	Location	Type
Javadoc: Missing comment for private declaration	Score.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 7	Java Problem
Javadoc: Missing comment for private declaration	Score.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 8	Java Problem
Javadoc: Missing comment for private declaration	Score.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 9	Java Problem
*/