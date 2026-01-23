package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import com.example.myapplication.entity.User
import com.example.myapplication.entity.Page
import com.example.myapplication.entity.Course
import com.example.myapplication.entity.Progress

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUser(userId: Long): User?

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>
}

@Dao
interface CourseDao {
    @Insert
    suspend fun insert(course: Course): Long

    @Update
    suspend fun update(course: Course)

    @Query("SELECT * FROM course WHERE id = :courseId")
    suspend fun getCourse(courseId: Long): Course?

    @Query("SELECT * FROM course WHERE is_load = 1")
    suspend fun getLoadedCourses(): List<Course>

    @Query("SELECT * FROM course")
    suspend fun getAllCourses(): List<Course>
}

@Dao
interface PageDao {
    @Insert
    suspend fun insert(page: Page): Long

    @Update
    suspend fun update(page: Page)

    @Query("SELECT * FROM page WHERE course_id = :courseId")
    suspend fun getPagesByCourse(courseId: Long): List<Page>

    @Query("SELECT * FROM page WHERE id = :pageId")
    suspend fun getPage(pageId: Long): Page?
}

@Dao
interface ProgressDao {
    @Insert
    suspend fun insert(progress: Progress): Long

    @Update
    suspend fun update(progress: Progress)

    @Query("SELECT * FROM progress")
    suspend fun getUserProgress(): List<Progress>
}