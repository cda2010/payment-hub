(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('EvenementOperation', EvenementOperation);

    EvenementOperation.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function EvenementOperation($resource, DateUtils,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'evenement-operations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'queryByCompteAutre': {method: 'GET',url:BASE_URL_API+'evenement/find-by-comptre-autre', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function(data) {
                    if (data) {
                        data = angular.fromJson(data);
                        // data.dateOperation = DateUtils.convertDateTimeFromServer(data.dateOperation);
                    }
                    return data;
                }
            },
            'update': { method: 'PUT' }
        });
    }
})();
