(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('UserControls', UserControls);

    UserControls.$inject = ['$resource','BASE_URL_API'];

    function UserControls ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'user-controls/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'resetUserPassword': { method: 'POST',ur:BASE_URL_API+'user-controls/reset-password', isArray: false},
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
