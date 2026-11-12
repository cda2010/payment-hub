(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Lien', Lien);

    Lien.$inject = ['$resource','BASE_URL_API'];

    function Lien ($resource,BASE_URL_API) {
        var resourceUrl =  'api/liens/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getLien': {method: 'GET', url:BASE_URL_API+'getLien-of-lien',isArray: false},
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
