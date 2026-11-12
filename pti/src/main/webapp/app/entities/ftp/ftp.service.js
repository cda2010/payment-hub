(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Ftp', Ftp);

    Ftp.$inject = ['$resource'];

    function Ftp ($resource) {
        var resourceUrl =  'api/ftps/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
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
