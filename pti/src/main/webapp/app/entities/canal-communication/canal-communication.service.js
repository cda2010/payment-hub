(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CanalCommunication', CanalCommunication);

    CanalCommunication.$inject = ['$resource'];

    function CanalCommunication ($resource) {
        var resourceUrl =  'api/canal-communications/:id';

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
