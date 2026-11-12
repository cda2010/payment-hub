(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserControlDetailController', UserControlDetailController);

    UserControlDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'UserControl'];

    function UserControlDetailController($scope, $rootScope, $stateParams, previousState, entity, UserControl) {
        var vm = this;

        vm.userControl = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:userControlUpdate', function(event, result) {
            vm.userControl = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
