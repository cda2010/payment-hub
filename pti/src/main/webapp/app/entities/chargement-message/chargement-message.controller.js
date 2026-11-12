(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementMessageController', ChargementMessageController);

    ChargementMessageController.$inject = ['$state', '$scope','ChargementMessage', 'AlertService', '$timeout','Upload','Notification','CancelAlert','LigneMessage','Principal'];

    function ChargementMessageController($state,$scope, ChargementMessage,  AlertService, $timeout,Upload,Notification,CancelAlert,LigneMessage,Principal) {

        var vm = this;

        vm.progressingBarAzeroCompte = progressingBarAzeroCompte;
        vm.ctrlbtnsave= true;
        vm.click=false;
        vm.afficheFichier = false;
        vm.selectedRow=null;
        vm.format=null;
        vm.clearMessage=clearMessage;
        vm.lireFichierMessage = lireFichierMessage;
        vm.getLigneMessage=getLigneMessage;
        vm.validerChrgmtMessage=validerChrgmtMessage;
        vm.supprimerChargementMessage=supprimerChargementMessage;
        vm.confirmValidation=confirmValidation;
        vm.page = 1;
        vm.page2=1;
        vm.totalItems = 0;
        vm.totalItems2 = 0;
        vm.itemsPerPage=20;
        vm.queryCount=0;
        vm.queryCount2=0;

        loadAll();
        function progressingBarAzeroCompte(file) {
            vm.progressionBar = 0;
            vm.fileName = file.name;
            vm.fil=file;
            vm.ctrlbtnsave= false;
        }
        function clearMessage (picFile){
            picFile = null;
            vm.progressionBar = 0;
            vm.fileName="";
            vm.ctrlbtnsave= true;
        }
        function lireFichierMessage(file) {
            var url ='api/upload/chargement_message/fichier';
            Upload.upload({
                url: url,
                data: {file: file}
            }).then(
                function (resp) {
                    console.log('reponse',resp);
                    loadAll();
                    if(resp.status===200) {
                        console.log(file);
                        $timeout(function () {
                            Notification.success('Fichier enregistrer avec succès','2000')
                        },2000)
                    } else {
                        var message = resp.header('X-partnerApp-alert');
                        Notification.error(message,'success')
                    }
                }, function (resp) {
                    console.log('erreur',resp);
                    Notification.error('erreur de chargement du fichier\n echec encor\n')
                }, function (evt) {
                    var progressPercentageCompte = parseInt(100.0 * evt.loaded / evt.total);
                    vm.progressionBar = progressPercentageCompte;
                }
            )

        }
        function getLigneMessage(chargementMessage,index) {
            vm.click = false;
            vm.defchargement=chargementMessage;
            console.log(chargementMessage);
            vm.selectedRow = index;
            LigneMessage.getLigneMessage({
                chargementId: chargementMessage.id,
                page:vm.page-1,
                size:vm.itemsPerPage
            }, function (response,headers) {
                console.log(response);
                vm.totalItems=headers('X-Total-Count');
                vm.queryCount = response.length;
                console.log('transaction',response);
                vm.canShowResultCount=true;
                vm.ligneMessage=response;
            })

        }

        function confirmValidation(chargement) {
            console.log('get',chargement);
            CancelAlert.confirm(function () {
                validerChrgmtMessage(chargement)
                    .success(function (datas) {
                        console.log('Yes', datas);
                        Notification.success('Opération éffectuée avec succès');
                        $state.reload('reglement-organisation-valide');
                    })
                    .error(function (datas) {
                        console.log('No', datas);
                        Notification.error('Echec de l\'opération');
                    })
            })
        }

        function validerChrgmtMessage(chargement) {
            vm.click = true;
            Principal.identity().then(function (account) {
                console.log(account);
                ChargementMessage.valider({
                    chargementId: chargement.id
                }, function (result, headers) {
                    console.log(result);
                    var message = headers('X-ptiApp-params');
                    Notification.success(message);
                    loadAll();
                })

            })
        }
        function supprimerChargementMessage() {
            var charg = "";
            if (confirm('Voulez vous supprimer ce fichier')) {
                LigneMessage.deleteChargement({id: vm.defchargement.id}, function (data) {
                    data = angular.fromJson(data);
                    console.log(data.values);
                    vm.defchargement = charg;
                    loadAll();
                    getLigneMessage(charg, index);
                });
            }
        }
        function loadAll () {
            if ($state.current.name==='chargement-message-valide'){
                vm.afficheFichier=true;

            }else{
                vm.afficheFichier=false;

            }
            //vm.afficheFichier = true;
            ChargementMessage.query({
                page:vm.page2-1,
                size:vm.itemsPerPage
            },function (data,headers) {
                console.log(data);
                vm.totalItems2=headers('X-Total-Count');
                vm.queryCount2 = data.length;
                vm.canShowResultCount2=true;
                vm.chargementMessages = data;
                console.log('totalItems2',vm.totalItems2);
            });
        }
        $scope.$watch("vm.page2", function (id) {
            loadAll();
        });
        $scope.$watch("vm.page", function (id) {
            getLigneMessage(vm.defchargement,vm.selectedRow);
        });

    }
})();
