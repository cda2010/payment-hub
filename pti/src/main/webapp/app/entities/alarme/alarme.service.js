(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Alarme', Alarme);

    Alarme.$inject = ['$resource'];

    function Alarme ($resource) {
        var resourceUrl =  'api/alarmes/:id';

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
