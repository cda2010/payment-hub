(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('GroupRepartition', GroupRepartition);

    GroupRepartition.$inject = ['$resource','BASE_URL_API'];


    function GroupRepartition ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'group-repartitions/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'findAll': { method: 'GET',url: BASE_URL_API+'group-repartitions/all', isArray: true},
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
