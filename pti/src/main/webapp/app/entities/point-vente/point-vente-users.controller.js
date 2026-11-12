(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PointVenteUsersController', PointVenteUsersController);

    PointVenteUsersController.$inject = ['$state','pointVente','$uibModalInstance','RequestUtils','PointVente', 'TableService', '$scope'];

    function PointVenteUsersController($state,pointVente,$uibModalInstance,RequestUtils,PointVente, TableService, $scope) {

        var vm = this;
        vm.clear = clear;
        vm.pointVente=pointVente;
        vm.userId=null;
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        vm.click=function(){
            console.log("clicl");
        }

        vm.userColumns=[{
            name: 'login',
            title: 'Login'
        },{
            name: 'lastName',
            title: 'Nom'
        },{
            name: 'firstName',
            title: 'Prenom'
        }];

        RequestUtils.OrganisationUsersWithoutPage.get({
        }, function(datas) {
            vm.users=datas;
            console.log('users',datas);
           angular.forEach(vm.users,function(user,key){
                if(user.pointVenteId==vm.pointVente.id){
                    vm.userId=user.id;
                  //  vm.usersPv.push(user);
                }
            });
        });
        
        /*
            Liste des utilisateurs d'un point de vente
            TAF
        */
        vm.save=function(){
            console.log("enregistrement des informations ");
            var datas={};
            var userIds=[];
            userIds.push(vm.userId);
            
            datas.userIds=userIds;
            datas.pointVenteId=pointVente.id;
            RequestUtils.PointVenteUsers.save(datas, function(datas){
                console.log("mise à jour des utilisateurs!");
                vm.clear();
                $state.go("point-vente");
            }, function(){
            });
            
        };
    }
})();
