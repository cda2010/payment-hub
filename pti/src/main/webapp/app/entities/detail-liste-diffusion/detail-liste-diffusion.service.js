(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('DetailListeDiffusion', DetailListeDiffusion);

    DetailListeDiffusion.$inject = ['$resource','BASE_URL_API'];

    function DetailListeDiffusion ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'detail-liste-diffusions/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByListe': { method: 'GET',url:BASE_URL_API+'detail-liste-diffusions-of-liste-diffusion/:id', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
