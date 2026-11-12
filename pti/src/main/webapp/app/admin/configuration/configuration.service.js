(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('JhiConfigurationService', JhiConfigurationService);

    JhiConfigurationService.$inject = ['$filter', '$http','BASE_URL'];

    function JhiConfigurationService ($filter, $http,BASE_URL) {
        var service = {
            get: get,
            getEnv: getEnv
        };

        return service;

        function get () {
            return $http.get(BASE_URL_API+'management/configprops').then(getConfigPropsComplete);

            function getConfigPropsComplete (response) {
                var properties = [];
                angular.forEach(response.data, function (data) {
                    properties.push(data);
                });
                var orderBy = $filter('orderBy');
                return orderBy(properties, 'prefix');
            }
        }

        function getEnv () {
            return $http.get(BASE_URL_API+'management/env').then(getEnvComplete);

            function getEnvComplete (response) {
                var properties = {};
                angular.forEach(response.data, function (val,key) {
                    var vals = [];
                    angular.forEach(val, function (v,k) {
                        vals.push({ key:k, val:v });
                    });
                    properties[key] = vals;
                });
                return properties;
            }
        }
    }
})();
