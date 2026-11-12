(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Document', Document);

    Document.$inject = ['$resource','BASE_URL_API'];

    function Document ($resource,BASE_URL_API) {
        var resourceUrl =  'api/documents/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getImage': {method: 'GET', url: BASE_URL_API + 'document/showImage' ,isArray: false},
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
