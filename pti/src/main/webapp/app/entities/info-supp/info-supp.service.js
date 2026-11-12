(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('InfoSupp', InfoSupp);

    InfoSupp.$inject = ['$resource','BASE_URL_API'];

    function InfoSupp ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'infosupp/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            // 'get': {
            //     url: BASE_URL_API + 'infosupp/libelle',
            //     method: 'GET',
            //     params: { id: '@id' },
            //     transformResponse: function (data) {
            //         if (data) {
            //             data = angular.fromJson(data);
            //         }
            //         return data;
            //     }
            // },
            'update': { method:'PUT' } ,
            'getLibelle': {
                url: BASE_URL_API + 'infosupp/libelle',
                method: 'GET',
                params: { id: '@id' },
                transformResponse: function (data) {
                    return data;
                }
            },
        });
    }
})();
