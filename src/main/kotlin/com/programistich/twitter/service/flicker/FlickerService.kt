package com.programistich.twitter.service.flicker

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.auth.Auth
import com.flickr4java.flickr.auth.Permission
import com.flickr4java.flickr.photos.Photo
import com.flickr4java.flickr.photos.PhotosInterface
import com.flickr4java.flickr.photos.SearchParameters
import com.programistich.twitter.configuration.flicker.FlickerConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream


@Service
@EnableConfigurationProperties(FlickerConfiguration::class)
class FlickerService(
    private val flickerConfiguration: FlickerConfiguration
) {

    @Value("\${flickr.key}")
    private lateinit var key: String

    @Value("\${flickr.secret}")
    private lateinit var secret: String

    fun findByText(text: String) {
        val flicker = Flickr(flickerConfiguration.key, flickerConfiguration.secret, REST())
        val requestContext = RequestContext.getRequestContext()
        val auth = Auth()
        auth.permission = Permission.READ
        auth.token = flickerConfiguration.token
        auth.tokenSecret = flickerConfiguration.tokensecret
        requestContext.auth = auth

        val photos: PhotosInterface = flicker.photosInterface
        val params = SearchParameters()
        params.media = "photos" // One of "photos", "videos" or "all"

        params.extras = Stream.of("media").collect(Collectors.toSet())
        params.text = text
        val results = photos.search(params, 5, 0)

        results.forEach(Consumer { p: Photo ->
            println(String.format("Title: %s", p.title))
            println(String.format("Media: %s", p.media))
            println(String.format("Original Video URL: %s", p.videoOriginalUrl))
        })

    }

}