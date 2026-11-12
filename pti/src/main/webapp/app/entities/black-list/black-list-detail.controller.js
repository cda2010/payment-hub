(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BlackListDetailController', BlackListDetailController);

    BlackListDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'BlackList', 'Client'];

    function BlackListDetailController($scope, $rootScope, $stateParams, previousState, entity, BlackList, Client) {
        var vm = this;

        vm.blackList = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:blackListUpdate', function(event, result) {
            vm.blackList = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
