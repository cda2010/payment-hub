(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HoraireAuthentificationDetailController', HoraireAuthentificationDetailController);

    HoraireAuthentificationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'HoraireAuthentification'];

    function HoraireAuthentificationDetailController($scope, $rootScope, $stateParams, previousState, entity, HoraireAuthentification) {
        var vm = this;

        vm.horaireAuthentification = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:horaireAuthentificationUpdate', function(event, result) {
            vm.horaireAuthentification = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
