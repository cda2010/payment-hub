(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('JhiMetricsService', JhiMetricsService);

    JhiMetricsService.$inject = ['$rootScope', '$http','BASE_URL'];

    function JhiMetricsService ($rootScope, $http,BASE_URL) {
        var service = {
            getMetrics: getMetrics,
            threadDump: threadDump
        };

        return service;

        function getMetrics () {
            return $http.get(BASE_URL+'management/metrics').then(function (response) {
                return response.data;
            });
        }

        function threadDump () {
            return $http.get(BASE_URL+'management/dump').then(function (response) {
                return response.data;
            });
        }
    }
})();
